/* (C) Copyright 2009-2013 CNRS (Centre National de la Recherche Scientifique).

Licensed to the CNRS under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The CNRS licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.

*/

/* Contributors:

Luc Hogie (CNRS, I3S laboratory, University of Nice-Sophia Antipolis) 
Aurelien Lancin (Coati research team, Inria)
Christian Glacet (LaBRi, Bordeaux)
David Coudert (Coati research team, Inria)
Fabien Crequis (Coati research team, Inria)
Grégory Morel (Coati research team, Inria)
Issam Tahiri (Coati research team, Inria)
Julien Fighiera (Aoste research team, Inria)
Laurent Viennot (Gang research-team, Inria)
Michel Syska (I3S, Université Cote D'Azur)
Nathann Cohen (LRI, Saclay) 
Julien Deantoin (I3S, Université Cote D'Azur, Saclay) 

*/
 
 package toools;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import toools.io.JavaResource;

public class Clazz
{
	public static <T> List<Class<T>> findImplementationsInTheSamePackage(
			Class<T> model)
	{
		return findImplementations(model, Clazz.listAllClasses(model.getPackage()));
	}

	public static <T> List<Class<T>> findImplementationsInPackage(Class<T> model,
			Package... pkg)
	{
		return findImplementations(model, Clazz.listAllClasses(pkg));
	}

	public static <T> List<Class<T>> findImplementations(Class<T> model)
	{
		return findImplementations(model, ClassPath.retrieveSystemClassPath());
	}

	public static <T> List<Class<T>> findImplementations(Class<T> model,
			ClassPath classpath)
	{
		return findImplementations(model, classpath.listAllClasses());
	}

	public static <T> List<Class<T>> findImplementations(Class<T> model,
			Iterable<Class<?>> classes)
	{
		List<Class<T>> implementations = new ArrayList<Class<T>>();

		for (Class<?> potentialImplementation : classes)
		{
			if (isAnImplementation(potentialImplementation, model))
			{
				implementations.add((Class<T>) potentialImplementation);
			}
		}

		return implementations;
	}

	public static boolean isAnImplementation(Class<?> potentialImplementation,
			Class<?> model)
	{
		return model.isAssignableFrom(potentialImplementation)
				&& Clazz.isInstantiable(potentialImplementation);
	}

	public static boolean classExists(String className)
	{
		try
		{
			Class.forName(className);
			return true;
		}
		catch (ClassNotFoundException e)
		{
			return false;
		}
		catch (NoClassDefFoundError e)
		{
			return false;
		}
	}

	public static boolean isAbstract(Class<?> thisClass)
	{
		return Modifier.isAbstract(thisClass.getModifiers());
	}

	public static boolean isInterface(Class<?> thisClass)
	{
		return Modifier.isInterface(thisClass.getModifiers());
	}

	public static boolean isConcrete(Class<?> thisClass)
	{
		return ! isInterface(thisClass) && ! isAbstract(thisClass);
	}

	public static <V> Constructor<V> findDefaultConstructor(Class<V> thisClass)
	{
		try
		{
			return thisClass.getConstructor(new Class[0]);
		}
		catch (SecurityException e)
		{
			return null;
		}
		catch (NoSuchMethodException e)
		{
			return null;
		}
	}

	public static boolean isInner(Class<?> thisClass)
	{
		return thisClass.getName().contains("$");
	}

	public static boolean isInstantiable(Class<?> thisClass)
	{
		return Modifier.isPublic(thisClass.getModifiers()) && isConcrete(thisClass)
				&& ! isInner(thisClass);
	}

	public static boolean isInstantiableWithoutArguments(Class<?> thisClass)
	{
		return isInstantiable(thisClass) && findDefaultConstructor(thisClass) != null;
	}

	public static Class findClass(String className) throws NoClassDefFoundError
	{
		try
		{
			return Class.forName(className);
		}
		catch (ClassNotFoundException e)
		{
			throw new NoClassDefFoundError(className);
		}
	}

	public static Class<?> findClassOrFail(String className)
	{
		if (className == null)
			throw new NullPointerException();

		try
		{
			return Class.forName(className);
		}
		catch (ClassNotFoundException e)
		{
			throw new NoClassDefFoundError(e.getMessage());
		}
	}

	public static <T> T makeInstanceOrFail(Class<T> clazz)
			throws ClassInstantiationException
	{
		if (clazz == null)
			throw new NullPointerException("cannot instantiate the null class");

		try
		{
			return clazz.newInstance();
		}
		catch (Throwable e)
		{
			if (e.getCause() == null)
			{
				throw new ClassInstantiationException(e);
			}
			else
			{
				throw new ClassInstantiationException(e.getCause());
			}
		}
	}

	public static <T> T makeInstance(Class<T> clazz)
	{
		try
		{
			return makeInstanceOrFail(clazz);
		}
		catch (ClassInstantiationException e)
		{
			return null;
		}
	}

	public static <T> T makeInstance(Constructor<T> constructor, Object... args)
	{
		try
		{
			constructor.setAccessible(true);
			return constructor.newInstance(args);
		}
		catch (Exception e)
		{
			throw new IllegalStateException(e.getCause() == null ? e : e.getCause());
		}
	}

	public static <T> List<T> makeSeveralInstances(Class<T> clazz, int numberOfInstances)
			throws ClassInstantiationException
	{
		List<T> c = new ArrayList<T>();

		while (numberOfInstances-- > 0)
		{
			c.add(makeInstance(clazz));
		}

		return c;
	}

	public static List<Class<?>> listAllClasses(Package... pkg)
	{
		List<Package> packages = Arrays.asList(pkg);
		List<Class<?>> classes = new ArrayList<Class<?>>();

		for (Class<?> clazz : ClassPath.retrieveSystemClassPath().listAllClasses())
		{
			if (packages.contains(clazz.getPackage()))
			{
				classes.add(clazz);
			}
		}

		return classes;
	}

	public static boolean isExecutable(Class<?> thisClass)
	{
		try
		{
			Method m = thisClass.getMethod("main",
					new Class[] { (new String[0]).getClass() });
			return (m.getModifiers() & Modifier.STATIC) != 0;
		}
		catch (Throwable e)
		{
			return false;
		}
	}

	public static ClassPath findClassContainer(String classname, ClassPath urls)
	{
		String resname = classname.replace('.', '/') + ".class";
		return JavaResource.findResourceContainer(resname, urls);
	}

	public static boolean hasDefaultConstructor(Class<? extends Object> c)
	{
		try
		{
			return c.getConstructor(new Class[0]) != null;
		}
		catch (SecurityException e)
		{
			throw new IllegalStateException(e);
		}
		catch (NoSuchMethodException e)
		{
			return false;
		}
	}

	public static ClassName getClassName(String name)
	{
		int p = name.lastIndexOf('.');

		if (p >= 0)
		{
			return new ClassName(name.substring(0, p), name.substring(p + 1));
		}
		else
		{
			return new ClassName(null, name);
		}
	}

	public static <T> T makeInstance(Class<T> c, Object[] parms)
	{
		Class[] types = new Class[parms.length];

		for (int i = 0; i < parms.length; ++i)
		{
			types[i] = parms[i].getClass();
		}

		Constructor<T> co = getConstructor(c, types);
		return makeInstance(co, parms);
	}

	public static <T> Constructor<T> getConstructor(Class<T> c, Class[] types)
	{
		try
		{
			return c.getConstructor(types);
		}
		catch (NoSuchMethodException | SecurityException e)
		{
			return null;
		}
	}

	public static <T> List<Constructor<T>> getConstructorsAccepting(Class<T> c,
			Object... parms)
	{
		// System.out.println("searching constructor " +
		// Clazz.toString(Clazz.getClasses(parms)) + " in class " + c);

		Class[] parmTypes = getClasses(parms);

		List<Constructor<T>> r = new ArrayList<>();

		for (Constructor co : c.getDeclaredConstructors())
		{
			if (isAssignableFrom(co.getParameterTypes(), parmTypes))
			{
				r.add(co);
			}
		}

		return r;
	}

	public static Class[] getClasses(Object... objects)
	{
		Class[] classes = new Class[objects.length];

		for (int i = 0; i < objects.length; ++i)
		{
			classes[i] = objects[i] == null ? null : objects[i].getClass();
		}

		return classes;
	}

	public static Map<Class, Class> class_primitives = new HashMap<Class, Class>();

	static
	{
		class_primitives.put(Integer.class, int.class);
		class_primitives.put(Long.class, long.class);
		class_primitives.put(Short.class, short.class);
		class_primitives.put(Double.class, double.class);
		class_primitives.put(Float.class, float.class);
		class_primitives.put(Character.class, char.class);
		class_primitives.put(Byte.class, byte.class);
		class_primitives.put(Boolean.class, boolean.class);
	}

	public static Map<Class, Class> primivite_class = new HashMap<Class, Class>();

	static
	{
		primivite_class.put(int.class, Integer.class);
		primivite_class.put(double.class, Double.class);
		primivite_class.put(short.class, Short.class);
		primivite_class.put(float.class, Float.class);
		primivite_class.put(long.class, Long.class);
		primivite_class.put(char.class, Character.class);
		primivite_class.put(byte.class, Byte.class);
		primivite_class.put(boolean.class, Boolean.class);
	}

	public static boolean isAssignableFrom(Class[] dest, Class[] src)
	{
		if (dest.length == src.length)
		{
			for (int i = 0; i < dest.length; ++i)
			{
				// the type is undefined
				if (src[i] == null)
				{
					// only a primitive type can't match
					// all object types can
					if (dest[i].isPrimitive())
					{
						return false;
					}
				}
				else
				{
					// both are primitive
					if (dest[i].isPrimitive() && src[i].isPrimitive())
					{
						if (dest[i] != src[i])
						{
							return false;
						}
					}
					// none are primitive
					else if ( ! dest[i].isPrimitive() && ! src[i].isPrimitive())
					{
						if ( ! dest[i].isAssignableFrom(src[i]))
						{
							return false;
						}
					}
					else if (dest[i].isPrimitive())
					{
						Class correspondingPrimitiveType = class_primitives.get(src[i]);

						if (correspondingPrimitiveType == null
								|| correspondingPrimitiveType != dest[i])
						{
							return false;
						}
					}
					else if (src[i].isPrimitive())
					{
						Class correspondingPrimitiveType = class_primitives.get(dest[i]);

						if (correspondingPrimitiveType == null
								|| correspondingPrimitiveType != src[i])
						{
							return false;
						}
					}
				}
			}

			return true;
		}
		else
		{
			return false;
		}
	}

	public static int computeDistance(Class[] src, Class[] dest)
	{
		if (src.length == dest.length)
		{
			int sum = 0;

			for (int i = 0; i < dest.length; ++i)
			{
				sum += computeDistance(src[i], dest[i]);
			}

			return sum;
		}
		else
		{
			return Integer.MAX_VALUE;
		}
	}

	public static int computeDistance(Class src, Class dest)
	{
		if (src == dest)
		{
			return 0;
		}
		else
		{
			List<Class> l = new ArrayList<>();

			for (Class i : src.getInterfaces())
			{
				l.add(i);
			}

			if (src.getSuperclass() != null)
			{
				l.add(src.getSuperclass());
			}

			int min = 100000;

			for (Class i : l)
			{
				int d = computeDistance(i, dest);

				if (d < min)
				{
					min = d;
				}
			}

			return min + 1;
		}
	}

	public static void main(String[] args)
	{
		System.out.println(computeDistance(List.class, Serializable.class));
	}

	public static String toString(Class[] classes)
	{
		return Arrays.asList(classes).toString();

	}

	/**
	 * Returns the size of the given class. This assumes that the class is
	 * either a primitive type or a class containing primitive types.
	 */
	public static int sizeOf(Class c)
	{
		if (c.isPrimitive())
		{
			if (c == byte.class || c == boolean.class)
			{
				return 1;
			}
			else if (c == char.class || c == short.class)
			{
				return 2;
			}
			else if (c == int.class || c == float.class)
			{
				return 4;
			}
			else if (c == long.class || c == double.class)
			{
				return 8;
			}
			else
				throw new IllegalStateException(c.getName());
		}
		else
		{
			int sizeof = 0;

			while (c != null)
			{
				for (Field f : c.getDeclaredFields())
				{
					if (f.getType().isPrimitive())
					{
						sizeof += sizeOf(f.getType());
					}
					else
					{
						return - 1;
					}
				}

				c = c.getSuperclass();
			}

			return sizeof;
		}
	}
}
