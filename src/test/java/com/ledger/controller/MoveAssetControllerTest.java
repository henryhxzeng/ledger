package com.ledger.controller;

import static org.hamcrest.CoreMatchers.containsString;

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
public class MoveAssetControllerTest {
	@Autowired
	MoveAssetController moveAssetController;

	@MockBean
	AccountAggregate acccountAggregate;
	@MockBean
	AccountProjection accountProjection;
	
	@Autowired
	MockMvc mockMvc;
	

	@Test
	public void testValidateRequestBody_EmptyRequest() throws Exception {
		String json = "{}";
		mockMvc.perform(MockMvcRequestBuilders.post("/api/moveAsset")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.status().isBadRequest())
		.andExpect(MockMvcResultMatchers.jsonPath("$.requests", Is.is("requests is mandatory")))
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	public void testValidateRequestBody_IdIsNotUUID() throws Exception {
		String json = "{ \"requests\":[{\"fromWalletId\": \"1162d6b9-3919-4f10-8b53-3702551fc248xxxx\", \"toWalletId\": \"0649699b-78f6-4ce9-af9a-79f3f765cf84\", \"amount\": 1.0}]}";
		mockMvc.perform(MockMvcRequestBuilders.post("/api/moveAsset")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.status().isBadRequest())
		.andExpect(MockMvcResultMatchers.content().string(containsString("requests[0].fromWalletId")))
		.andExpect(MockMvcResultMatchers.content().string(containsString("fromWalletId must be a UUID")))
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	public void testValidateRequestBody_invalidAmount() throws Exception {
		String json = "{ \"requests\":[{\"fromWalletId\": \"1162d6b9-3919-4f10-8b53-3702551fc248\", \"toWalletId\": \"0649699b-78f6-4ce9-af9a-79f3f765cf84\", \"amount\": -1.0}]}";
		mockMvc.perform(MockMvcRequestBuilders.post("/api/moveAsset")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.status().isBadRequest())
		.andExpect(MockMvcResultMatchers.content().string(containsString("requests[0].amount")))
		.andExpect(MockMvcResultMatchers.content().string(containsString("amount is mandatory and must be positive")))
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	public void testOK() throws Exception {
		String json = "{ \"requests\":[{\"fromWalletId\": \"1162d6b9-3919-4f10-8b53-3702551fc248\", \"toWalletId\": \"0649699b-78f6-4ce9-af9a-79f3f765cf84\", \"amount\": 1.0}]}";
		mockMvc.perform(MockMvcRequestBuilders.post("/api/moveAsset")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.jsonPath("$.message", Is.is("Move asset is processing in async mode!")))
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
}
