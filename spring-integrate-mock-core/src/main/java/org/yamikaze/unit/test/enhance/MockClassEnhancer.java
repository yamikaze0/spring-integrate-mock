package org.yamikaze.unit.test.enhance;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.yamikaze.unit.test.mock.Constants;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-07-22 15:25
 * cs:off
 */
public class MockClassEnhancer implements ClassEnhancer {

    /**
     * The origin class bytes.
     */
    private final byte[] classFileByte;

    /**
     * The class bytes after enhanced.
     */
    private byte[] enhanceBytes;

    public MockClassEnhancer(byte[] classFileByte) {
        this.classFileByte = classFileByte;
    }

    @Override
    public byte[] enhanceClass() {
        if (enhanceBytes != null) {
            return enhanceBytes;
        }

        ClassReader classReader = new ClassReader(classFileByte);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor classVisitor = new MockClassVisitor(Opcodes.ASM7, classWriter);
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
        enhanceBytes = classWriter.toByteArray();

        return enhanceBytes;
    }

    static class MockClassVisitor extends ClassVisitor {

        private String className;

        MockClassVisitor(int api, ClassVisitor classVisitor) {
            super(api, classVisitor);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.className = name;
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            final MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            MethodVisitor  methodVisitor = new JSRInlinerAdapter(mv, access, name, descriptor, signature, exceptions);

            ModifyStaticMethodVisitor modifyStaticMethodByteCode = new ModifyStaticMethodVisitor(Opcodes.ASM7, methodVisitor, access, name, descriptor);
            modifyStaticMethodByteCode.className = this.className;

            return modifyStaticMethodByteCode;

        }
    }

    static class BoxingTypeWrapper {
        private final String boxingTypeName;

        private final String unboxingMethodName;

        private final String unboxingMethodDesc;

        public BoxingTypeWrapper(String boxingTypeName, String unboxingMethodName, String unboxingMethodDesc) {
            this.boxingTypeName = boxingTypeName;
            this.unboxingMethodName = unboxingMethodName;
            this.unboxingMethodDesc = unboxingMethodDesc;
        }
    }

    static class ModifyStaticMethodVisitor extends AdviceAdapter {

        private final boolean isClinitMethod;

        private final String methodName;

        private String className;

        private final Type[] argumentTypes;

        private final boolean isConstrcutor;

        public ModifyStaticMethodVisitor(int api, MethodVisitor mv, int access, String name, String desc) {
            super(api, mv, access, name, desc);
            this.isClinitMethod = "<clinit>".equals(name);
            this.methodName = name;
            this.isConstrcutor = "<init>".equals(name);
            this.argumentTypes = Type.getArgumentTypes(methodDesc);

        }

        private static final Map<String, BoxingTypeWrapper> CLASS_BOXING = new HashMap<>(16);

        static {
            CLASS_BOXING.put("int", new BoxingTypeWrapper("java/lang/Integer", "intValue", "()I"));
            CLASS_BOXING.put("short", new BoxingTypeWrapper("java/lang/Short", "shortValue", "()S"));
            CLASS_BOXING.put("byte", new BoxingTypeWrapper("java/lang/Byte", "byteValue", "()B"));
            CLASS_BOXING.put("long", new BoxingTypeWrapper("java/lang/Long", "longValue", "()J"));
            CLASS_BOXING.put("double", new BoxingTypeWrapper("java/lang/Double", "doubleValue", "()D"));
            CLASS_BOXING.put("float", new BoxingTypeWrapper("java/lang/Float", "floatValue", "()F"));
            CLASS_BOXING.put("char", new BoxingTypeWrapper("java/lang/Character", "charValue", "()C"));
            CLASS_BOXING.put("boolean", new BoxingTypeWrapper("java/lang/Boolean", "booleanValue", "()Z"));
        }

        private static BoxingTypeWrapper getBoxingTypeWrapper(String className) {
            return CLASS_BOXING.get(className);
        }

        private int getSlots(Type[] types) {
            // no params
            if (types == null || types.length == 0) {
                return 0;
            }

            int slots = 0;

            for (Type type : types) {
                if  (type.getSort() == Type.DOUBLE
                        || type.getSort() == Type.LONG)   {
                    slots += 2;
                } else {
                    slots++;
                }
            }

            return slots;
        }

        @Override
        protected void onMethodEnter() {
            if (isClinitMethod || isConstrcutor) {
                return;
            }

            /*
             * if (GlobalConfig.getSwitch()) {
             *     Object result = InternalMockUtils.findMockResult(className, methodName, allArguments);
             *     if (result != InternalMockUtils.NO_MOCK) {
             *         return result;
             *     }
             * }
             *
             */
            int slots = getSlots(argumentTypes);
            if (!Modifier.isStatic(methodAccess)) {
                // non-static methods first slot is this.
                slots++;
            }

            visitMethodInsn(Opcodes.INVOKESTATIC, Constants.CONFIG_CLASSNAME, Constants.MOCK_ENABLED_METHOD, "()Z", false);
            Label label = new Label();
            mv.visitJumpInsn(Opcodes.IFEQ, label);
            Type returnType = Type.getReturnType(methodDesc);

            //need push args
            visitLdcInsn(className);
            visitLdcInsn(methodName);
            visitLdcInsn(methodDesc);
            loadArgArray();

            visitMethodInsn(Opcodes.INVOKESTATIC, Constants.INTERNAL_MOCK_CLASSNAME, "findMockResult", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;", false);
            visitVarInsn(Opcodes.ASTORE, slots);
            visitVarInsn(Opcodes.ALOAD, slots);

            visitFieldInsn(GETSTATIC, Constants.INTERNAL_MOCK_CLASSNAME, "NO_MOCK", "Ljava/lang/Object;");
            mv.visitJumpInsn(Opcodes.IF_ACMPEQ, label);

            int returnOpcodes = Opcodes.RETURN;
            if (returnType != Type.VOID_TYPE) {
                visitVarInsn(Opcodes.ALOAD, slots);

                //非Object返回值需要强转
                if (!JAVA_LANG_OBJECT.equals(returnType.getClassName()))  {
                    String internalName = returnType.getInternalName();
                    boolean isArray = false;
                    if (internalName.startsWith(ARRAY_DIMENSION_CHAR)) {
                        isArray = true;
                    }

                    BoxingTypeWrapper boxingType = getBoxingTypeWrapper(returnType.getClassName());

                    if (isArray) {
                        mv.visitTypeInsn(Opcodes.CHECKCAST, internalName);
                        mv.visitTypeInsn(Opcodes.CHECKCAST, internalName);
                    } else if (boxingType != null) {
                        mv.visitTypeInsn(Opcodes.CHECKCAST, boxingType.boxingTypeName);
                        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, boxingType.boxingTypeName, boxingType.unboxingMethodName, boxingType.unboxingMethodDesc, false);
                    } else {
                        mv.visitTypeInsn(Opcodes.CHECKCAST, returnType.getInternalName());
                    }
                }

                returnOpcodes = returnType.getOpcode(Opcodes.IRETURN);
            }


            mv.visitInsn(returnOpcodes);

            mv.visitLabel(label);
            mv.visitFrame(F_SAME, 0, null, 0, null);

        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            super.visitMaxs(maxStack + 100 , maxLocals);
        }
    }
}
