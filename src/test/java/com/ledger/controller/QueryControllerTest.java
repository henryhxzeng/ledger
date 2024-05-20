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
public class QueryControllerTest {
	@Autowired
	QueryController queryController;

	@MockBean
	AccountAggregate acccountAggregate;
	@MockBean
	AccountProjection accountProjection;
	
	@Autowired
	MockMvc mockMvc;
	
	@Test
	public void getEntity_InvalidId() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/query/entity/{id}", "6baaaded-9823-40dc-a93b-6aa54d2042a5xxxx")
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.status().isBadRequest())
		.andExpect(MockMvcResultMatchers.jsonPath("$.message", Is.is("entityId must be a UUID")))
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	public void getEntity_testOK() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/query/entity/{id}", "6baaaded-9823-40dc-a93b-6aa54d2042a5")
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	public void getAccount_InvalidId() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/query/account/{id}", "6baaaded-9823-40dc-a93b-6aa54d2042a5xxxx")
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.status().isBadRequest())
		.andExpect(MockMvcResultMatchers.jsonPath("$.message", Is.is("accountId must be a UUID")))
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	public void getAccount_testOK() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/query/account/{id}", "6baaaded-9823-40dc-a93b-6aa54d2042a5")
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	
	
	@Test
	public void getWallet_InvalidId() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/query/wallet/{id}", "6baaaded-9823-40dc-a93b-6aa54d2042a5xxxx")
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.status().isBadRequest())
		.andExpect(MockMvcResultMatchers.jsonPath("$.message", Is.is("walletId must be a UUID")))
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	public void getWallet_testOK() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/query/wallet/{id}", "6baaaded-9823-40dc-a93b-6aa54d2042a5")
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	public void getMovement_InvalidId() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/query/movement/{id}", "6baaaded-9823-40dc-a93b-6aa54d2042a5xxxx")
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.status().isBadRequest())
		.andExpect(MockMvcResultMatchers.jsonPath("$.message", Is.is("id must be a UUID")))
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	public void getMovement_testOK() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/query/movement/{id}", "6baaaded-9823-40dc-a93b-6aa54d2042a5")
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	public void getHistoricalBalance_EmptyRequest() throws Exception {
		String json = "{}";
		mockMvc.perform(MockMvcRequestBuilders.post("/api/query/walletHistoricalBalance")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.status().isBadRequest())
		.andExpect(MockMvcResultMatchers.jsonPath("$.id", Is.is("id is mandatory")))
		.andExpect(MockMvcResultMatchers.jsonPath("$.datetime", Is.is("datetime is mandatory")))
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	public void getHistoricalBalance_InvalidId() throws Exception {
		String json = "{\"id\": \"6baaaded-9823-40dc-a93b-6aa54d2042a5xxxx\",\"datetime\": \"2024-05-15 13:48:00+0000\"}";
		mockMvc.perform(MockMvcRequestBuilders.post("/api/query/walletHistoricalBalance")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.status().isBadRequest())
		.andExpect(MockMvcResultMatchers.jsonPath("$.id", Is.is("id must be a UUID")))
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	public void getHistoricalBalance_InvalidDatetime() throws Exception {
		String json = "{\"id\": \"6baaaded-9823-40dc-a93b-6aa54d2042a5\",\"datetime\": \"2024-05-15 13:48:00\"}";
		mockMvc.perform(MockMvcRequestBuilders.post("/api/query/walletHistoricalBalance")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.status().isBadRequest())
		.andExpect(MockMvcResultMatchers.jsonPath("$.datetime", Is.is("datetime must be in format of yyyy-MM-dd HH:mm:ssZ. For example: 2024-05-19 13:48:00+0000")))
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	public void getHistoricalBalance_testOK() throws Exception {
		String json = "{\"id\": \"6baaaded-9823-40dc-a93b-6aa54d2042a5\",\"datetime\": \"2024-05-15 13:48:00+0000\"}";
		mockMvc.perform(MockMvcRequestBuilders.post("/api/query/walletHistoricalBalance")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
}
