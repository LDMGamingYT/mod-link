package net.ldm.mod_link.ftp;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.text.Text;
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

			int responseCode = ftpClient.getReplyCode();

			if (!FTPReply.isPositiveCompletion(responseCode)) {
				LOG.info("Connection failed: " + responseCode);
				return;
			}

			boolean success = ftpClient.login(ModLinkFtpServer.USERNAME, "");
			if (!success) {
				ftpClient.disconnect();
				LOG.info("Could not login to the server: " + responseCode);
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
				client.setScreen(new MessageScreen(Text.literal("Downloading " + file.toFile().getName())));
				ftpClient.retrieveFile(ftpFile.getName(), outputStream);
				LOG.info("Downloaded {}", file.toFile().getName());
				client.setScreen(new MultiplayerScreen(new TitleScreen()));
			}
		}
	}

	// TODO: 2023-09-13 Fix this
	public static ModLinkFtpClient fromIp(String ip) {
		try {
			// Check if the IP contains a port number (e.g., "127.0.0.1:25565")
			/*int colonIndex = ip.lastIndexOf(':');
			String ipAddress;
			int port = 0;

			if (colonIndex > 0 && colonIndex < ip.length() - 1) {
				// Extract IP address and port
				ipAddress = ip.substring(0, colonIndex);
				port = Integer.parseInt(ip.substring(colonIndex + 1));
			} else {
				// No port specified, use the entire input as the IP address
				ipAddress = ip;
			}

			// Validate and convert the IP address
			InetAddress address = InetAddress.getByName(ipAddress);*/

			// Create and return the ModLinkFtpClient instance
			return new ModLinkFtpClient(ip, 0);
		} catch (NumberFormatException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return String.format("FTP Client: ftp://%s@%s", ModLinkFtpServer.USERNAME, ftpClient.getRemoteAddress());
	}
}
