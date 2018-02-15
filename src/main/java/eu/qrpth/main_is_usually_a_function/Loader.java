package eu.qrpth.main_is_usually_a_function;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Loader extends ClassLoader {
    final private static String MAIN_CLASS_NAME = "eu.qrpth.main_is_usually_a_function.Main";

    private Class<?> main_class;

    public Loader(ClassLoader parent) {
        super(parent);
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (name.equals(MAIN_CLASS_NAME)) {
            synchronized (getClassLoadingLock(name)) {
                if (main_class == null) {
                    try {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        copy(getResourceAsStream(name.replace(".", "/") + ".class"), baos);
                        byte[] clsData = baos.toByteArray();
                        Class<?> cls = new SupplementaryLoader()
                                .defineClass(name , clsData);
                        Field data = cls.getField("main");

                        clsData = convertToBytes((long[]) data.get(null));
                        main_class = defineClass(name, clsData, 0, clsData.length);
                    } catch (Exception e) {
                        throw new RuntimeException("error while loading class", e);
                    }
                }

                if (resolve) {
                    this.resolveClass(main_class);
                }

                return main_class;
            }
        }
        return super.loadClass(name, resolve);
    }

    private void copy(InputStream in, OutputStream out) throws IOException {
        int n;
        byte[] buffer = new byte[1024];
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
    }

    private byte[] convertToBytes(long[] data) {
        byte[] res = new byte[data.length * 8];
        ByteBuffer buffer = ByteBuffer.wrap(res).order(ByteOrder.LITTLE_ENDIAN);
        for (long l: data) {
            buffer.putLong(l);
        }
        return res;
    }

    private static class SupplementaryLoader extends ClassLoader {
        public SupplementaryLoader() {
            super();
        }

        public Class<?> defineClass(String name, byte[] data) {
            return defineClass(name, data, 0, data.length);
        }
    }
}
