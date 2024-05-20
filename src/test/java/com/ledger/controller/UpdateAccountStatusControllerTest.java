package com.ledger.controller;

import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.ledger.cqrs.aggregate.AccountAggregate;
import com.ledger.cqrs.projection.AccountProjection;

@ExtendWith(SpringExtension.class)
@WebMvcTest
@AutoConfigureMockMvc
public class UpdateAccountStatusControllerTest {

	@Autowired
	UpdateAccountStatusController updateAccountStatusController;
	
	@MockBean
	AccountAggregate acccountAggregate;
	@MockBean
	AccountProjection accountProjection;
	
	@Autowired
	MockMvc mockMvc;
	
	@Test
	public void testValidateRequestBody_emptyRequest() throws Exception {
		String json = "{}";
		mockMvc.perform(MockMvcRequestBuilders.post("/api/updateAccountStatus")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.status().isBadRequest())
		.andExpect(MockMvcResultMatchers.jsonPath("$.id", Is.is("id is mandatory")))
		.andExpect(MockMvcResultMatchers.jsonPath("$.status", Is.is("status is mandatory")))
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	public void testValidateRequestBody_IdIsNotUUID() throws Exception {
		String json = "{\"id\" :\"530ffc78-d9de-4491-a5ef-a8b0efd4a6deXXXXX\",\"status\": \"CLOSED\"}";
		mockMvc.perform(MockMvcRequestBuilders.post("/api/updateAccountStatus")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.status().isBadRequest())
		.andExpect(MockMvcResultMatchers.jsonPath("$.id", Is.is("id must be a UUID")))
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	public void testValidateRequestBody_InvalidStatus() throws Exception {
		String json = "{\"id\" :\"530ffc78-d9de-4491-a5ef-a8b0efd4a6de\",\"status\": \"ABCD\"}";
		mockMvc.perform(MockMvcRequestBuilders.post("/api/updateAccountStatus")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.status().isBadRequest())
		.andExpect(MockMvcResultMatchers.jsonPath("$.status", Is.is("invalid value for this field")))
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	public void testOK() throws Exception {
		String json = "{\"id\" :\"530ffc78-d9de-4491-a5ef-a8b0efd4a6de\",\"status\": \"CLOSED\"}";
		mockMvc.perform(MockMvcRequestBuilders.post("/api/updateAccountStatus")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.jsonPath("$.message", Is.is("Account status update request is processing in async mode!")))
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
}
