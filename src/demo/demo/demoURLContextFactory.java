package demo.demo;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

public class demoURLContextFactory implements ObjectFactory {

	private static DemoContext demoContext = new DemoContext();

	@Override
	public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment)
			throws Exception {
		System.out.println(environment);
		return demoContext;
	}

}
