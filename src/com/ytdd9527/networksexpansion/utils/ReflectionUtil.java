package com.ytdd9527.networksexpansion.utils;

import com.balugaq.netex.utils.Debug;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import lombok.Generated;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ReflectionUtil {
   public static boolean setValue(Object object, String field, @Nullable Object value) {
      try {
         Field declaredField = getField(object.getClass(), field);
         if (declaredField == null) {
            throw new NoSuchFieldException(field);
         } else {
            declaredField.setAccessible(true);
            declaredField.set(object, value);
            return true;
         }
      } catch (IllegalAccessException | NoSuchFieldException var4) {
         Debug.trace(var4);
         return false;
      }
   }

   @Nullable
   public static Field getField(Class<?> clazz, String fieldName) {
      while (clazz != Object.class) {
         for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().equals(fieldName)) {
               return field;
            }
         }

         clazz = clazz.getSuperclass();
      }

      return null;
   }

   public static <T> boolean setStaticValue(Class<T> clazz, String field, @Nullable Object value) {
      try {
         Field declaredField = getField(clazz, field);
         if (declaredField == null) {
            throw new NoSuchFieldException(field);
         } else {
            declaredField.setAccessible(true);
            declaredField.set(null, value);
            return true;
         }
      } catch (IllegalAccessException | NoSuchFieldException var4) {
         Debug.trace(var4);
         return false;
      }
   }

   @Nullable
   public static Object getStaticValue(Class<?> clazz, String field) {
      try {
         Field declaredField = getField(clazz, field);
         if (declaredField == null) {
            throw new NoSuchFieldException(field);
         } else {
            declaredField.setAccessible(true);
            return declaredField.get(null);
         }
      } catch (IllegalAccessException | NoSuchFieldException var3) {
         Debug.trace(var3);
         return null;
      }
   }

   @Nullable
   public static <T> T getStaticValue(Class<?> clazz, String field, Class<T> cast) {
      try {
         Field declaredField = getField(clazz, field);
         if (declaredField == null) {
            throw new NoSuchFieldException(field);
         } else {
            declaredField.setAccessible(true);
            return (T)declaredField.get(null);
         }
      } catch (IllegalAccessException | NoSuchFieldException var4) {
         Debug.trace(var4);
         return null;
      }
   }

   @Nullable
   public static Method getMethod(Class<?> clazz, String methodName, boolean noargs) {
      while (clazz != Object.class) {
         for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName) && (!noargs || method.getParameterTypes().length == 0)) {
               return method;
            }
         }

         clazz = clazz.getSuperclass();
      }

      return getMethod(clazz, methodName);
   }

   @Nullable
   public static Method getMethod(Class<?> clazz, String methodName) {
      while (clazz != Object.class) {
         for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
               return method;
            }
         }

         clazz = clazz.getSuperclass();
      }

      return null;
   }

   @Nullable
   public static Class<?> getClass(Class<?> clazz, String className) {
      while (clazz != Object.class) {
         if (clazz.getSimpleName().equals(className)) {
            return clazz;
         }

         clazz = clazz.getSuperclass();
      }

      return null;
   }

   @Nullable
   public static <T> T getValue(Object object, String fieldName, Class<T> cast) {
      try {
         Field field = getField(object.getClass(), fieldName);
         if (field != null) {
            field.setAccessible(true);
            return (T)field.get(object);
         } else {
            return null;
         }
      } catch (IllegalAccessException var4) {
         Debug.trace(var4);
         return null;
      }
   }

   @Nullable
   public static Object getValue(Object object, String fieldName) {
      try {
         Field field = getField(object.getClass(), fieldName);
         if (field != null) {
            field.setAccessible(true);
            return field.get(object);
         } else {
            return null;
         }
      } catch (IllegalAccessException var3) {
         Debug.trace(var3);
         return null;
      }
   }

   @Nullable
   public static <T, V> T getProperty(Object o, Class<V> clazz, String fieldName) throws IllegalAccessException {
      Field field = getField(clazz, fieldName);
      if (field != null) {
         boolean b = field.canAccess(o);
         field.setAccessible(true);
         Object result = field.get(o);
         field.setAccessible(b);
         return (T)result;
      } else {
         return null;
      }
   }

   @Nullable
   public static Pair<Field, Class<?>> getDeclaredFieldsRecursively(Class<?> clazz, String fieldName) {
      try {
         Field field = clazz.getDeclaredField(fieldName);
         field.setAccessible(true);
         return new Pair(field, clazz);
      } catch (Exception var3) {
         clazz = clazz.getSuperclass();
         return clazz == null ? null : getDeclaredFieldsRecursively(clazz, fieldName);
      }
   }

   @Nullable
   public static Constructor<?> getConstructor(Class<?> clazz, @Nullable Class<?>... parameterTypes) {
      try {
         return clazz.getDeclaredConstructor(parameterTypes);
      } catch (NoSuchMethodException var3) {
         Debug.trace(var3);
         return null;
      }
   }

   @Nullable
   public static Object invokeMethod(Object object, String methodName, @Nullable Object... args) {
      try {
         Method method;
         if (args == null) {
            method = getMethod(object.getClass(), methodName, 1);
         } else {
            boolean containsNull = false;
            Object[] var5 = args;
            int var6 = args.length;
            int var7 = 0;

            while (true) {
               if (var7 < var6) {
                  Object arg = var5[var7];
                  if (arg != null) {
                     var7++;
                     continue;
                  }

                  containsNull = true;
               }

               if (containsNull) {
                  method = getMethod(object.getClass(), methodName, args.length);
               } else {
                  method = getMethod(object.getClass(), methodName, Arrays.stream(args).filter(Objects::nonNull).map(Object::getClass).toArray(Class[]::new));
               }
               break;
            }
         }

         if (method != null) {
            method.setAccessible(true);
            return method.invoke(object, args);
         }
      } catch (IllegalAccessException | InvocationTargetException var9) {
         Debug.trace(var9);
      }

      return null;
   }

   @Nullable
   public static Method getMethod(Class<?> clazz, String methodName, @Range(from = 0L,to = 32767L) int parameterCount) {
      while (clazz != Object.class) {
         for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName) && method.getParameterTypes().length == parameterCount) {
               return method;
            }
         }

         clazz = clazz.getSuperclass();
      }

      return null;
   }

   @Nullable
   public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
      while (clazz != Object.class) {
         for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName) && method.getParameterTypes().length == parameterTypes.length) {
               boolean match = true;

               for (int i = 0; i < parameterTypes.length; i++) {
                  if (warpClass(method.getParameterTypes()[i]) != warpClass(parameterTypes[i])) {
                     match = false;
                     break;
                  }
               }

               if (!match) {
                  match = true;

                  for (int ix = 0; ix < parameterTypes.length; ix++) {
                     if (!method.getParameterTypes()[ix].isAssignableFrom(parameterTypes[ix])) {
                        match = false;
                        break;
                     }
                  }
               }

               if (match) {
                  return method;
               }
            }
         }

         clazz = clazz.getSuperclass();
      }

      return null;
   }

   @Nullable
   public static Object invokeStaticMethod(Class<?> clazz, String methodName, @Nullable Object... args) {
      try {
         Method method;
         if (args == null) {
            method = getMethod(clazz, methodName, 1);
         } else {
            boolean containsNull = false;
            Object[] var5 = args;
            int var6 = args.length;
            int var7 = 0;

            while (true) {
               if (var7 < var6) {
                  Object arg = var5[var7];
                  if (arg != null) {
                     var7++;
                     continue;
                  }

                  containsNull = true;
               }

               if (containsNull) {
                  method = getMethod(clazz, methodName, args.length);
               } else {
                  method = getMethod(clazz, methodName, Arrays.stream(args).filter(Objects::nonNull).map(Object::getClass).toArray(Class[]::new));
               }
               break;
            }
         }

         if (method != null) {
            method.setAccessible(true);
            return method.invoke(null, args);
         }
      } catch (IllegalAccessException | InvocationTargetException var9) {
         Debug.trace(var9);
      }

      return null;
   }

   public static Class<?> warpClass(Class<?> clazz) {
      Class var10000;
      if (!clazz.isPrimitive()) {
         var10000 = clazz;
      } else {
         String var1 = clazz.getName();
         switch (var1) {
            case "boolean":
               var10000 = Boolean.class;
               break;
            case "byte":
               var10000 = Byte.class;
               break;
            case "char":
               var10000 = Character.class;
               break;
            case "double":
               var10000 = Double.class;
               break;
            case "float":
               var10000 = Float.class;
               break;
            case "int":
               var10000 = Integer.class;
               break;
            case "long":
               var10000 = Long.class;
               break;
            case "short":
               var10000 = Short.class;
               break;
            default:
               var10000 = clazz;
         }
      }

      return var10000;
   }

   @Nullable
   public static Class<?> getClass(String... classNames) {
      for (String className : classNames) {
         try {
            return Class.forName(className);
         } catch (ClassNotFoundException var6) {
         }
      }

      return null;
   }

   @Generated
   private ReflectionUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
