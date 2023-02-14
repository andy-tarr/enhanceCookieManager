package us.abstracta.jmeter.javadsl.cli;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

public class JmdslConfig {

  public static final String CONFIG_OPTION = "--config";
  public static final String DEFAULT_CONFIG_FILE = ".jmdsl.yml";

  private RecorderCommand recorder;

  // This is required by jackson for deserialization
  public JmdslConfig() {
  }

  public JmdslConfig(RecorderCommand recorder) {
    this.recorder = recorder;
  }

  public static JmdslConfig fromConfigFile(File configFile) throws IOException {
    if (configFile.getPath().equals(DEFAULT_CONFIG_FILE) && !configFile.exists()) {
      return null;
    }
    return new ObjectMapper(new YAMLFactory())
        .setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
        .readValue(configFile, JmdslConfig.class);
  }

  public void updateWithDefaultsFrom(JmdslConfig other) {
    if (other == null) {
      return;
    }
    applyDefaultsFromTo(other.recorder, this.recorder);
  }

  private static void applyDefaultsFromTo(Object defaults, Object target) {
    Arrays.stream(target.getClass().getDeclaredFields())
        .filter(f -> f.isAnnotationPresent(Option.class) || f.isAnnotationPresent(Parameters.class))
        .filter(f -> {
          Object prevVal = getField(f, target);
          return prevVal == null || prevVal instanceof List && (((List) prevVal).isEmpty());
        })
        .forEach(f -> setField(f, target, getField(f, defaults)));
    Arrays.stream(target.getClass().getDeclaredFields())
        .filter(f -> f.isAnnotationPresent(ArgGroup.class))
        .forEach(f -> applyDefaultsFromTo(getField(f, defaults), getField(f, target)));
  }

  private static Object getField(Field field, Object instance) {
    try {
      field.setAccessible(true);
      return field.get(instance);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private static void setField(Field field, Object instance, Object value) {
    try {
      field.set(instance, value);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

}