package toools.io.ser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import toools.reflect.Clazz;
import toools.text.TextUtilities;

public class GSONSerializer<E> extends Serializer<E> {
	public static final GSONSerializer instance = new GSONSerializer();
	public String msg;
	public boolean useHolder = false;

	static class ClassAdapter extends TypeAdapter<Class> {
		@Override
		public Class read(JsonReader reader) throws IOException {
			String classname = reader.nextString();
			classname = classname.replace("-", "$");
			return Clazz.findClass(classname);
		}

		@Override
		public void write(JsonWriter writer, Class c) throws IOException {
			writer.value(c.getName().replace("$", "-"));
		}
	}

	static final Gson gson;

	static {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Class.class, new ClassAdapter());
		builder.setPrettyPrinting();
		gson = builder.create();
	}

	public static class Holder<E> {
		E value;
		String type;
	}

	@Override
	public E read(InputStream is) throws IOException {
		Reader r = new InputStreamReader(is);
		Holder<E> m = gson.fromJson(r, Holder.class);
		return m.value;
	}

	@Override
	public void write(E o, OutputStream os) throws IOException {

		Object oo;

		if (useHolder) {
			Holder<E> holder = new Holder<>();
			holder.value = o;
			holder.type = TextUtilities.getNiceClassName(o.getClass());
			oo = holder;
		} else {
			oo = o;
		}

		String json = gson.toJson(oo);
		os.write(json.getBytes());
	}

	@Override
	public String getMIMEType() {
		return "Google GSON";
	}

	public static void main(String[] args) {
		String in = "salut";
		byte[] bytes = GSONSerializer.instance.toBytes(in);
		Object out = GSONSerializer.instance.fromBytes(bytes);
		System.out.println(in.equals(out));
	}	@Override
	public boolean isBinary() {
		return false;
	}

}
