package com.hortonworks.iotas.streams.layout.storm;

import com.hortonworks.iotas.streams.layout.ConfigFieldValidation;
import com.hortonworks.iotas.streams.layout.TopologyLayoutConstants;
import com.hortonworks.iotas.streams.layout.exception.BadTopologyLayoutException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Abstract implementation of FluxComponent interface. Child classes just
 * need to implement generateComponent method using conf and update referenced
 * components and component variables
 */
public abstract class AbstractFluxComponent implements FluxComponent {
    protected enum ArgsType {
        NONE
    }


    // conf is the map representing the configuration parameters for this
    // storm component picked by the user. For eg kafka component will have
    // zkUrl, topic, etc.
    protected Map<String, Object> conf;
    private boolean isGenerated;
    protected List<Map<String, Object>> referencedComponents = new
            ArrayList<Map<String, Object>>();
    protected Map<String, Object> component = new LinkedHashMap<String, Object>();
    protected final UUID UUID_FOR_COMPONENTS = UUID.randomUUID();
//  TODO: to be fixed after catalog rest client is refactored
//  protected CatalogRestClient catalogRestClient;

    @Override
    public void withCatalogRootUrl(String catalogRootUrl) {
//        this.catalogRestClient = new CatalogRestClient(catalogRootUrl);
    }

    @Override
    public void withConfig (Map<String, Object> conf) {
        this.conf = conf;
    }

    @Override
    public List<Map<String, Object>> getReferencedComponents () {
        if (!isGenerated) {
            generateComponent();
            isGenerated = true;
        }
        return referencedComponents;
    }

    @Override
    public Map<String, Object> getComponent () {
        if (!isGenerated) {
            generateComponent();
            isGenerated = true;
        }
        return component;
    }

    @Override
    public void validateConfig () throws BadTopologyLayoutException {
        String[] fieldNames = {TopologyLayoutConstants.JSON_KEY_PARALLELISM};
        Long[] mins = {1l};
        Long[] maxes = {Long.MAX_VALUE};
        this.validateLongFields(fieldNames, false, mins, maxes);
    }

    // private helper method to generate referenced components and the component
    abstract protected void generateComponent ();

    protected void addParallelismToComponent () {
        Integer parallelism;
        if ((parallelism = (Integer) conf.get(TopologyLayoutConstants
                .JSON_KEY_PARALLELISM)) != null) {
            component.put(StormTopologyLayoutConstants.YAML_KEY_PARALLELISM, parallelism);
        }
    }

    protected void addToComponents (Map<String, Object> componentMap) {
        if (componentMap == null ) {
            return;
        }
        referencedComponents.add(componentMap);
    }

    protected Map createComponent (String id, String className, List properties, List constructorArgs, List configMethods) {
        Map component = new LinkedHashMap();
        component.put(StormTopologyLayoutConstants.YAML_KEY_ID, id);
        component.put(StormTopologyLayoutConstants.YAML_KEY_CLASS_NAME, className);
        if (properties != null && properties.size() > 0) {
            component.put(StormTopologyLayoutConstants.YAML_KEY_PROPERTIES, properties);
        }
        if (constructorArgs != null && constructorArgs.size() > 0) {
            component.put(StormTopologyLayoutConstants.YAML_KEY_CONSTRUCTOR_ARGS, constructorArgs);
        }
        if (configMethods != null && configMethods.size() > 0) {
            component.put(StormTopologyLayoutConstants.YAML_KEY_CONFIG_METHODS, configMethods);
        }
        return component;
    }

    protected List getPropertiesYaml (String[] propertyNames) {
        checkNotEmpty(propertyNames);
        List properties = new ArrayList();
        for (int i = 0; i < propertyNames.length; ++i) {
            Object value = conf.get(propertyNames[i]);
            if (value != null) {
                Map propertyMap = new LinkedHashMap();
                propertyMap.put(StormTopologyLayoutConstants.YAML_KEY_NAME,
                        propertyNames[i]);
                propertyMap.put(StormTopologyLayoutConstants.YAML_KEY_VALUE,
                          value);
                properties.add(propertyMap);
            }
        }
        return properties;
    }

    protected List getConstructorArgsYaml (String[] constructorArgNames) {
        checkNotEmpty(constructorArgNames);
        List constructorArgs = new ArrayList();
        for (int i = 0; i < constructorArgNames.length; ++i) {
            Object value = conf.get(constructorArgNames[i]);
            if (value != null) {
                constructorArgs.add(value);
            }
        }
        return constructorArgs;
    }

    protected List getConfigMethodsYaml (String[] configMethodNames, String[] configKeys) {
        checkNotEmptyAndSize(configMethodNames, configKeys);

        List<String> nonNullConfigMethodNames = new ArrayList<String>();
        List values = new ArrayList();

        for (int i = 0; i < configKeys.length; ++i) {
            if (conf.get(configKeys[i]) != null) {
                nonNullConfigMethodNames.add(configMethodNames[i]);
                values.add(conf.get(configKeys[i]));
            }
        }
        List configMethods = getConfigMethodsYaml(nonNullConfigMethodNames.toArray(new String[0]), values.toArray());
        return configMethods;
    }

    private void checkNotEmptyAndSize(Object[] arg1, Object[] arg2) {
        checkNotEmpty(arg1);
        checkNotEmpty(arg2);
        if ( arg1.length != arg2.length ) {
            throw new IllegalArgumentException("Argument arrays of unequal length");
        }
    }

    private void checkNotEmpty(Object[] arg1) {
        if ( arg1.length==0 ) {
            throw new IllegalArgumentException("Argument array cannot be empty");
        }
    }

    protected List getConfigMethodsYaml (String[] configMethodNames, Object[] values) {
        checkNotEmptyAndSize(configMethodNames, values);
        List configMethods = new ArrayList();
        for (int i = 0; i < values.length; ++i) {
            Map configMethod = new LinkedHashMap();
            configMethod.put(StormTopologyLayoutConstants.YAML_KEY_NAME, configMethodNames[i]);
            List methodArgs = new ArrayList();
            Object value = values[i];
            if(value != ArgsType.NONE) {
                methodArgs.add(value);
            }
            configMethod.put(StormTopologyLayoutConstants.YAML_KEY_ARGS, methodArgs);
            configMethods.add(configMethod);
        }
        return configMethods;
    }

    protected Map getRefYaml (String refId) {
        Map ref = new LinkedHashMap();
        ref.put(StormTopologyLayoutConstants.YAML_KEY_REF, refId);
        return ref;
    }

    protected List getConfigMethodWithRefArg (String[] configMethodNames,
                                             String[] refIds) {
        checkNotEmptyAndSize(configMethodNames, refIds);
        List configMethods = new ArrayList();
        for (int i = 0; i < refIds.length; ++i) {
            Map configMethod = new LinkedHashMap();
            configMethod.put(StormTopologyLayoutConstants.YAML_KEY_NAME,
                    configMethodNames[i]);
            List methodArgs = new ArrayList();
            Map refMap = new HashMap();
            refMap.put(StormTopologyLayoutConstants.YAML_KEY_REF, refIds[i]);
            methodArgs.add(refMap);
            configMethod.put(StormTopologyLayoutConstants.YAML_KEY_ARGS, methodArgs);
            configMethods.add(configMethod);
        }

        return configMethods;
    }

    // validate boolean fields based on if they are required or not. Meant to
    // be called from base classes that need to validate
    protected void validateBooleanFields (String[] fieldNames, boolean areRequiredFields) throws BadTopologyLayoutException {
        this.validateBooleanFields(fieldNames, areRequiredFields, conf);
    }

    // Overloaded version of above method since we need it for NotificationBolt and perhaps other components in future
    protected void validateBooleanFields (String[] fieldNames, boolean areRequiredFields, Map<String, Object> conf) throws BadTopologyLayoutException {
        for (int i = 0; i < fieldNames.length; ++i) {
            String fieldName = fieldNames[i];
            Object value = conf.get(fieldName);
            boolean isValid = true;
            if (areRequiredFields) {
                // validate no matter what for required fields
                if (!ConfigFieldValidation.isBoolean(value)) {
                    isValid = false;
                }
            } else {
                // for optional fields validate only if user updated the
                // default value which means UI put it in json
                if ((value != null) && !ConfigFieldValidation.isBoolean(value)) {
                    isValid = false;
                }
            }
            if (!isValid) {
                throw new BadTopologyLayoutException(String.format(TopologyLayoutConstants.ERR_MSG_MISSING_INVALID_CONFIG, fieldName));
            }
        }
    }

    // validate string fields based on if they are required or not. Meant to
    // be called from base classes that need to validate
    protected void validateStringFields (String[] fieldNames, boolean areRequiredFields) throws BadTopologyLayoutException {
        this.validateStringFields(fieldNames, areRequiredFields, conf);
    }

    // Overloaded version of above method since we need it for NotificationBolt and perhaps other components in future
    protected void validateStringFields (String[] fieldNames, boolean areRequiredFields, Map<String, Object> conf) throws BadTopologyLayoutException {
        for (String fieldName: fieldNames) {
            Object value = conf.get(fieldName);
            boolean isValid = true;
            if (areRequiredFields) {
                // validate no matter what for required fields
                if (!ConfigFieldValidation.isStringAndNotEmpty(value)) {
                    isValid = false;
                }
            } else {
                // for optional fields validate only if user updated the
                // default value which means UI put it in json
                if ((value != null) && !ConfigFieldValidation.isStringAndNotEmpty(value)) {
                    isValid = false;
                }
            }
            if (!isValid) {
                throw new BadTopologyLayoutException(String.format(TopologyLayoutConstants.ERR_MSG_MISSING_INVALID_CONFIG, fieldName));
            }
        }
    }

    // validate byte fields based on if they are required or not and their
    // valid range. Meant to // be called from base classes that need to validate
    protected void validateByteFields (String[] fieldNames, boolean
            areRequiredFields, Byte[] mins, Byte[] maxes) throws
            BadTopologyLayoutException {
        if ((fieldNames == null) || (fieldNames.length != mins.length) ||
                (fieldNames.length != maxes.length)) {
            return;
        }
        for (int i = 0; i < fieldNames.length; ++i) {
            String fieldName = fieldNames[i];
            Object value = conf.get(fieldName);
            Byte min = mins[i];
            Byte max = maxes[i];
            boolean isValid = true;
            if (areRequiredFields) {
                // validate no matter what for required fields
                if (!ConfigFieldValidation.isByteAndInRange(value, min, max)) {
                    isValid = false;
                }
            } else {
                // for optional fields validate only if user updated the
                // default value which means UI put it in json
                if ((value != null) && !ConfigFieldValidation.isByteAndInRange(value, min, max)) {
                    isValid = false;
                }
            }
            if (!isValid) {
                throw new BadTopologyLayoutException(String.format(TopologyLayoutConstants.ERR_MSG_MISSING_INVALID_CONFIG, fieldName));
            }
        }
    }

    // validate short fields based on if they are required or not and their
    // valid range. Meant to // be called from base classes that need to validate
    protected void validateShortFields (String[] fieldNames, boolean
            areRequiredFields, Short[] mins, Short[] maxes) throws
            BadTopologyLayoutException {
        if ((fieldNames == null) || (fieldNames.length != mins.length) ||
                (fieldNames.length != maxes.length)) {
            return;
        }
        for (int i = 0; i < fieldNames.length; ++i) {
            String fieldName = fieldNames[i];
            Object value = conf.get(fieldName);
            Short min = mins[i];
            Short max = maxes[i];
            boolean isValid = true;
            if (areRequiredFields) {
                // validate no matter what for required fields
                if (!ConfigFieldValidation.isShortAndInRange(value, min, max)) {
                    isValid = false;
                }
            } else {
                // for optional fields validate only if user updated the
                // default value which means UI put it in json
                if ((value != null) && !ConfigFieldValidation.isShortAndInRange(value, min, max)) {
                    isValid = false;
                }
            }
            if (!isValid) {
                throw new BadTopologyLayoutException(String.format(TopologyLayoutConstants.ERR_MSG_MISSING_INVALID_CONFIG, fieldName));
            }
        }
    }

    // validate integer fields based on if they are required or not and their
    // valid range. Meant to // be called from base classes that need to validate
    protected void validateIntegerFields (String[] fieldNames, boolean areRequiredFields, Integer[] mins, Integer[] maxes) throws BadTopologyLayoutException {
        this.validateIntegerFields(fieldNames, areRequiredFields, mins, maxes, conf);
    }

    // Overloaded version of above method since we need it for NotificationBolt and perhaps other components in future
    protected void validateIntegerFields (String[] fieldNames, boolean areRequiredFields, Integer[] mins, Integer[] maxes, Map<String, Object> conf) throws
            BadTopologyLayoutException {
        if ((fieldNames == null) || (fieldNames.length != mins.length) ||
                (fieldNames.length != maxes.length)) {
            return;
        }
        for (int i = 0; i < fieldNames.length; ++i) {
            String fieldName = fieldNames[i];
            Object value = conf.get(fieldName);
            Integer min = mins[i];
            Integer max = maxes[i];
            boolean isValid = true;
            if (areRequiredFields) {
                // validate no matter what for required fields
                if (!ConfigFieldValidation.isIntAndInRange(value, min, max)) {
                    isValid = false;
                }
            } else {
                // for optional fields validate only if user updated the
                // default value which means UI put it in json
                if ((value != null) && !ConfigFieldValidation.isIntAndInRange(value, min, max)) {
                    isValid = false;
                }
            }
            if (!isValid) {
                throw new BadTopologyLayoutException(String.format(TopologyLayoutConstants.ERR_MSG_MISSING_INVALID_CONFIG, fieldName));
            }
        }
    }

    // validate long fields based on if they are required or not and their
    // valid range. Meant to // be called from base classes that need to validate
    protected void validateLongFields (String[] fieldNames, boolean
            areRequiredFields, Long[] mins, Long[] maxes) throws
            BadTopologyLayoutException {
        if ((fieldNames == null) || (fieldNames.length != mins.length) ||
                (fieldNames.length != maxes.length)) {
            return;
        }
        for (int i = 0; i < fieldNames.length; ++i) {
            String fieldName = fieldNames[i];
            Object value = conf.get(fieldName);
            Long min = mins[i];
            Long max = maxes[i];
            boolean isValid = true;
            if (areRequiredFields) {
                // validate no matter what for required fields
                if (!ConfigFieldValidation.isLongAndInRange(value, min, max)) {
                    isValid = false;
                }
            } else {
                // for optional fields validate only if user updated the
                // default value which means UI put it in json
                if ((value != null) && !ConfigFieldValidation.isLongAndInRange(value, min, max)) {
                    isValid = false;
                }
            }
            if (!isValid) {
                throw new BadTopologyLayoutException(String.format(TopologyLayoutConstants.ERR_MSG_MISSING_INVALID_CONFIG, fieldName));
            }
        }
    }

    protected void validateFloatOrDoubleFields (String[] fieldNames, boolean
            areRequiredFields) throws BadTopologyLayoutException {
        for (int i = 0; i < fieldNames.length; ++i) {
            String fieldName = fieldNames[i];
            Object value = conf.get(fieldName);
            boolean isValid = true;
            if (areRequiredFields) {
                // validate no matter what for required fields
                if (!ConfigFieldValidation.isFloatOrDouble(value)) {
                    isValid = false;
                }
            } else {
                // for optional fields validate only if user updated the
                // default value which means UI put it in json
                if ((value != null) && !ConfigFieldValidation.isFloatOrDouble(value)) {
                    isValid = false;
                }
            }
            if (!isValid) {
                throw new BadTopologyLayoutException(String.format(TopologyLayoutConstants.ERR_MSG_MISSING_INVALID_CONFIG, fieldName));
            }
        }
    }

    protected List<Object> makeConstructorArgs(String... keys) {
        List<Object> args = new ArrayList<>();
        for (String key : keys) {
            addArg(args, key);
        }
        return args;
    }

    protected void addArg(final List<Object> args, String key) {
        Object value = conf.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Value for key '" + key + "' not found in config");
        }
        args.add(value);
    }

    protected void addArg(final List<Object> args, String key, Object defaultValue) {
        Object value = conf.get(key);
        if (value != null) {
            args.add(value);
        } else {
            args.add(defaultValue);
        }
    }

    protected void addArg(List<Object> args, Map ref) {
        args.add(ref);
    }
}
