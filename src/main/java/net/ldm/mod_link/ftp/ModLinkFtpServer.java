package net.ldm.mod_link.ftp;

import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.filesystem.nativefs.NativeFileSystemFactory;
import org.apache.ftpserver.ftplet.*;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ModLinkFtpServer {
	public static final String USERNAME = "modlink";
	public static final int PORT = 25555;
	private final FtpServer server;
	public static final Logger LOG = LogManager.getLogger(ModLinkFtpServer.class);

	public ModLinkFtpServer(String path) {
		FtpServerFactory serverFactory = new FtpServerFactory();

		ListenerFactory factory = new ListenerFactory();
		factory.setPort(PORT);

		factory.setDataConnectionConfiguration(new DataConnectionConfigurationFactory().createDataConnectionConfiguration());

		FileSystemFactory fileSystemFactory = new NativeFileSystemFactory() {
			@Override
			public FileSystemView createFileSystemView(User user) throws FtpException {
				FileSystemView view = super.createFileSystemView(user);
				view.changeWorkingDirectory(path);
				return view;
			}
		};

		serverFactory.addListener("default", factory.createListener());
		serverFactory.setFileSystem(fileSystemFactory);

		// Setup user manager
		PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
		UserManager userManager = userManagerFactory.createUserManager();

		BaseUser user = new BaseUser();
		user.setName(USERNAME);
		user.setHomeDirectory(path);

		List<Authority> authorities = new ArrayList<>();
		user.setAuthorities(authorities);

		try {
			userManager.save(user);
		} catch (FtpException e) {
			e.printStackTrace();
		}

		serverFactory.setUserManager(userManager);

		server = serverFactory.createServer();
		LOG.info("Created Mod Link FTP server on port {}", PORT);
	}

	public void start() {
		try {
			server.start();
			LOG.info("Started Mod Link FTP server on port {}", PORT);
		} catch (FtpException e) {
			throw new RuntimeException(e);
		}
	}
}
