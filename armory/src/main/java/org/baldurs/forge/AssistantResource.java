package org.baldurs.forge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.baldurs.forge.builder.ModPackager;
import org.baldurs.forge.builder.NewModModel;
import org.baldurs.forge.chat.ChatFrameService;
import org.baldurs.forge.context.ChatContext;
import org.baldurs.forge.services.EquipmentDB;
import org.baldurs.forge.services.LibraryService;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/assistant")
public class AssistantResource {
	private static final Logger LOG = Logger.getLogger( AssistantResource.class);


	@Inject
	LibraryService library;

	@Inject
	EquipmentDB equipmentDB;

	@Inject
	ChatFrameService chat;
	

	@POST
	@Path("/chat")
	@Produces(MediaType.TEXT_PLAIN)
	public ChatContext chat(ChatContext context) throws Exception {

		Log.info("CHAT: " + context.memoryId());
		chat.chat(context);
		return context;
	}

	@POST
	@Path("/upload-pak")
	@Produces(MediaType.TEXT_HTML)
	public Response uploadpak(@RestForm("pak") FileUpload file, @QueryParam("filename") String filename) throws Exception {
		LOG.info("Uploading file: " + filename);
		try {
			LOG.info("Uploading file: " + file.uploadedFile());
			InputStream fileInputStream = new FileInputStream(file.uploadedFile().toFile());
			if (fileInputStream == null) {
				return Response.status(Status.BAD_REQUEST)
					.entity("{\"error\": \"No file provided\"}")
					.build();
			}

			// Use provided filename or generate unique one if not provided
			if (filename == null || filename.isEmpty()) {
				filename = "upload_" + UUID.randomUUID().toString() + ".pak";
			}

			// Create uploads directory if it doesn't exist
			java.nio.file.Path uploadsDir = library.modsPath();
			if (!Files.exists(uploadsDir)) {
				Files.createDirectories(uploadsDir);
			}

			// Save the file
			java.nio.file.Path filePath = uploadsDir.resolve(filename);
			Files.copy(fileInputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

			LOG.info("File uploaded successfully: " + filePath.toString());
			String msg = equipmentDB.uploadMod(filePath);

			// Return success response
			return Response.ok()
				.entity(msg)
				.build();

		} catch (IOException e) {
			LOG.error("Error uploading file: " + e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
				.entity("{\"error\": \"Failed to upload file: " + e.getMessage() + "\"}")
				.build();
		}
	}

	@Inject
	ModPackager modPackager;

	@POST
	@Path("/package-mod")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response packageMod(NewModModel newMod) throws Exception {
		LOG.info("Packaging mod");
		File file = modPackager.packageMod(newMod);
		return Response.ok(file)
			.header("Content-Disposition", "attachment; filename=" + file.getName())
			.build();
	}





}
