
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Protocol.Command;
import redis.clients.jedis.commands.ProtocolCommand;

public class MainClass2 {

	public static void main(String[] args) throws NamingException, IOException {
		jedisSentinel();
	}

	public static void aa() throws IOException {
		Path link = FileSystems.getDefault().getPath("D:\\renxinwei\\BrowserSwingLink.rar");
		Path target = FileSystems.getDefault().getPath("D:\\renxinwei\\apache-activemq-5.15.9-bin.zip");
		Files.createSymbolicLink(link, target);
	}

	public static void jedisSentinel() throws IOException {
		Set<String> sentinels = new HashSet<String>();
		sentinels.add("101.37.13.4:26379");
		sentinels.add("101.37.13.4:26380");
		sentinels.add("101.37.13.4:26381");
		JedisSentinelPool jsp = new JedisSentinelPool("mymaster", sentinels, "wrA*we!#@i5ojoI34UAP1");
		Jedis jedis = jsp.getResource();
		System.out.println(jedis.get("aab"));
		System.out.println(jedis.evalsha(jedis.scriptLoad("return redis.call('info')")));
		jedis.close();
		jsp.close();
	}
}
