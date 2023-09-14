package net.ldm.mod_link.ftp;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
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

			// TODO: 2023-09-13 the workdir is temp
			boolean changedRemoteDir = ftpClient.changeWorkingDirectory("E:\\Logan\\Temp\\modlink");
			if (!changedRemoteDir) {
				LOG.info("Failed to change working directory");
				return;
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void download(Path to) throws IOException {
		for (FTPFile ftpFile: ftpClient.listFiles()) {
			Path file = to.resolve(ftpFile.getName());
			Files.createFile(file);
			try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file.toFile()))) {
				boolean isFileRetrieved = ftpClient.retrieveFile(file.toString(), outputStream);
				LOG.info("Downloaded {}", file.toFile().getName());
			}
		}
	}
}
