package org.yamikaze.unit.test.junit.parameterized;

import org.yamikaze.unit.test.spi.ExtensionFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-08-14 15:02
 */
public class ArgumentFactoryProxy implements ArgumentFactory {

    @Override
    public List<Arguments> loadArguments(String fileLocation) {

        ArgumentFactory adaptiveArgumentFactory = null;
        
        List<ArgumentFactory> extensions = ExtensionFactory.getExtensions(ArgumentFactory.class);
        if (extensions != null && !extensions.isEmpty()) {
            adaptiveArgumentFactory = findAdaptiveAndHighPriorityFactory(fileLocation, extensions);
        }

        if (adaptiveArgumentFactory == null && fileLocation.endsWith(PlainTextArgumentFactory.SUFFIX)){
            adaptiveArgumentFactory = PlainTextArgumentFactory.INSTANCE;
        }
        
        if (adaptiveArgumentFactory == null) {
            throw new IllegalStateException("can't find adaptive argument parser for " + fileLocation);
        }

        return adaptiveArgumentFactory.loadArguments(fileLocation);
    }

    private ArgumentFactory findAdaptiveAndHighPriorityFactory(String fileLocation, List<ArgumentFactory> extensions) {

        List<ArgumentFactory> supportedArgumentFactories = new ArrayList<>();
        for (ArgumentFactory argumentFactory : extensions) {
            if (argumentFactory instanceof SupportedArgumentFactory) {
                if (((SupportedArgumentFactory)argumentFactory).supported(fileLocation)) {
                    supportedArgumentFactories.add(argumentFactory);
                }
            }
        }

        if (supportedArgumentFactories.isEmpty()) {
            return null;
        }

        // Because extensions is a sorted list.
        return supportedArgumentFactories.get(0);
    }
}
