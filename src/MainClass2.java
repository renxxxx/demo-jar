
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class MainClass2 {

	public static void main(String[] args) throws NamingException {
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
		Context cntxt = new InitialContext(env);
		cntxt.bind("a", "aaa");
		System.out.println(cntxt.lookup("a"));
	}

}
