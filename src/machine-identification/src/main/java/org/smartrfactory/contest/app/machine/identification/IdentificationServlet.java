package org.smartrfactory.contest.app.machine.identification;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

public class IdentificationServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private final MachineIdentificationApp app;

	public IdentificationServlet(final MachineIdentificationApp app) {
		this.app = app;
	}
	
	/**
	 * Trigger identification start
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		final StringBuilder sb = new StringBuilder();
		BufferedReader reader = req.getReader();
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append('\n');
			}
		} finally {
			reader.close();
		}
		final JSONObject json = new JSONObject(sb.toString());
		if (json.has("meterId") && json.has("startIdentification")) {
			final String meterId = json.getString("meterId");
			final String response = app.startIdentification(meterId);
			if (response != null) 
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
			else {
				MachineIdentificationApp.logger.info("Identification started for meter {}", meterId);
				resp.setStatus(HttpServletResponse.SC_OK);
			}
		}
		else {
			MachineIdentificationApp.logger.warn("Invalid request {}",json);
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters. Expecting 'meterId' and 'startIdentification'");
		}
	}

}
