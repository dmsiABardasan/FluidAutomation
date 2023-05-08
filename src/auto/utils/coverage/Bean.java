package auto.utils.coverage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public abstract class Bean implements Comparable<Bean>, Serializable {
	private static final long serialVersionUID = 1L;

	@Override
	public int compareTo(Bean bean) {
		return toString().compareTo(bean.toString());
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		boolean equal = false;
		if (obj instanceof Bean) {
			equal = toString().equals(obj.toString());
		}
		return equal;
	}

	@Override
	public abstract String toString();

	public static void serialize(Serializable entries, File file) {
		file.getParentFile().mkdirs();
		try {
			FileOutputStream fileOut = new FileOutputStream(file);
			ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
			objOut.writeObject(entries);
			objOut.close();
			fileOut.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T deserialize(File file) {
		T persistedEntries = null;
		try {
			FileInputStream fileIn = new FileInputStream(file);
			ObjectInputStream objIn = new ObjectInputStream(fileIn);
			persistedEntries = (T) objIn.readObject();
			objIn.close();
			fileIn.close();
		} catch (IOException | ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		return persistedEntries;
	}

}