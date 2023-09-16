package net.ldm.mod_link.ftp;

import net.ldm.mod_link.screen.PromptScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModLinkFtpClient {
	public static final Logger LOG = LogManager.getLogger(ModLinkFtpClient.class);
	private final FTPClient ftpClient;

	public ModLinkFtpClient(String ip, int port) {
		ftpClient = new FTPClient();

		try {
			ftpClient.connect(ip, port);
			int replyCode = ftpClient.getReplyCode();

			if (!FTPReply.isPositiveCompletion(replyCode)) {
				LOG.info("Connection failed: " + replyCode);
				return;
			}

			ftpClient.enterLocalActiveMode();

			boolean success = ftpClient.login(ModLinkFtpServer.USERNAME, "");
			if (!success) {
				ftpClient.disconnect();
				LOG.info("Could not login to the server: " + replyCode);
				return;
			}

			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			ftpClient.enterLocalPassiveMode();

			// If any issues arise, add back the "FTP client work dir" to the user's temp dir

		} catch (ConnectException e) {
			LOG.warn("Could not connect to FTP server, ignoring", e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void download(Path to, MinecraftClient client) throws IOException {
		for (FTPFile ftpFile: ftpClient.listFiles()) {
			Path file = to.resolve(ftpFile.getName());
			Files.deleteIfExists(file);
			Files.createFile(file);
			try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file.toFile()))) {
				client.setScreen(new PromptScreen("Downloading " + file.toFile().getName()));
				ftpClient.retrieveFile(ftpFile.getName(), outputStream);
				LOG.info("Downloaded {}", file.toFile().getName());
				client.setScreen(new MultiplayerScreen(new TitleScreen()));
			}
		}
	}

	public static ModLinkFtpClient fromServerAddress(String serverAddress, int port) {
		return new ModLinkFtpClient(ServerAddress.parse(serverAddress).getAddress(), port);
	}

	@Override
	public String toString() {
		return String.format("FTP Client: ftp://%s@%s", ModLinkFtpServer.USERNAME, ftpClient.getRemoteAddress());
	}
}
