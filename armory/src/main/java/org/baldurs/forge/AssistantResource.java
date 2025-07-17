package org.baldurs.forge;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.baldurs.forge.agents.ForgeAgent;
import org.baldurs.forge.agents.MetadataAgent;
import org.baldurs.forge.model.Equipment;
import org.baldurs.forge.model.EquipmentModel;
import org.baldurs.forge.nli.ToolBoxNLI;
import org.baldurs.forge.nli.ToolBoxNLIInvoker;
import org.baldurs.forge.toolbox.BoostService;
import org.baldurs.forge.toolbox.EquipmentDB;
import org.baldurs.forge.toolbox.LibraryService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
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
    ForgeAgent forgeAgent;


	@Inject
	MetadataAgent metadataFinderAgent;

	@Inject
	LibraryService library;

	@Inject
	EquipmentDB equipmentDB;


	@Inject
	@ToolBoxNLI({EquipmentDB.class, LibraryService.class, BoostService.class})
	ToolBoxNLIInvoker assistantCommandService;

	

	/**
	 * Executes a natural language query and returns data in JSON format.
	 */
	@GET
	@Path("/json")
	@Produces(MediaType.TEXT_PLAIN)
	public String queryToJson(@QueryParam("query") String query) throws Exception {
        LOG.info("QUERY: " + query);
		return assistantCommandService.execute(query);
	}


	/**
	 * Executes a natural language query and feeds back data into the LLM to provide a natural language response.
	 */
	@GET
	@Path("/ask")
	@Produces(MediaType.TEXT_PLAIN)
	public Response naturalLanguage(@QueryParam("query") String query) throws Exception {
		List<EquipmentModel> items = equipmentDB.search(query);

        String response = "";
        if (items.isEmpty()) {
            response = "I couldn't find any items that match your query.";
        } else {
           response = forgeAgent.queryEquipment(query, EquipmentModel.toJson(items));
		   LOG.info("RESPONSE:\n " + response + "\n");
        }
		return Response.ok(response).build();
	}

	/**
	 * Handles file upload for mod files.
	 */
	@POST
	@Path("/upload")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadFile(InputStream fileInputStream, @QueryParam("filename") String filename) throws Exception {
		try {
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
			library.uploadMod(filePath);

			// Return success response
			return Response.ok()
				.entity("{\"message\": \"File uploaded successfully\", \"filename\": \"" + filename + "\"}")
				.build();

		} catch (IOException e) {
			LOG.error("Error uploading file: " + e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
				.entity("{\"error\": \"Failed to upload file: " + e.getMessage() + "\"}")
				.build();
		}
	}

}
