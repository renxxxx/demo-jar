
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class MainClass2 {

	public static void main(String[] args) throws NamingException, IOException {
		aa();
	}

	public static void aa() throws IOException {
		Path link = FileSystems.getDefault().getPath("D:\\renxinwei\\BrowserSwingLink.rar");
		Path target = FileSystems.getDefault().getPath("D:\\renxinwei\\apache-activemq-5.15.9-bin.zip");
		Files.createSymbolicLink(link, target);
	}
}
