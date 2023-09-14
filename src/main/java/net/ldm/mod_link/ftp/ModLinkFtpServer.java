package net.ldm.mod_link.ftp;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.filesystem.nativefs.NativeFileSystemFactory;
import org.apache.ftpserver.ftplet.*;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;

import java.util.ArrayList;
import java.util.List;

public class ModLinkFtpServer {
	public static final String USERNAME = "modlink";
	private final FtpServer server;

	public ModLinkFtpServer(int port, String path) {
		FtpServerFactory serverFactory = new FtpServerFactory();

		ListenerFactory factory = new ListenerFactory();
		factory.setPort(port);

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
	}

	public void start() {
		try {
			server.start();
		} catch (FtpException e) {
			throw new RuntimeException(e);
		}
	}
}
