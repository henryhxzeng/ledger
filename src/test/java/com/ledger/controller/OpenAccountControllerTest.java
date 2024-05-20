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
public class OpenAccountControllerTest {

	@Autowired
	OpenAccountController openAccountController;
	
	@MockBean
	AccountAggregate acccountAggregate;
	@MockBean
	AccountProjection accountProjection;
	
	@Autowired
	MockMvc mockMvc;
	
	@Test
	public void testValidateRequestBody_emptyRequest() throws Exception {
		String json = "{}";
		mockMvc.perform(MockMvcRequestBuilders.post("/api/openAccount")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.status().isBadRequest())
		.andExpect(MockMvcResultMatchers.jsonPath("$.entityName", Is.is("entityName is mandatory")))
		.andExpect(MockMvcResultMatchers.jsonPath("$.account", Is.is("account is mandatory")))
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}

	@Test
	public void testValidateRequestBody_entityNameIsEmpty() throws Exception {
		String json = "{\"entityName\" : \"\",\"account\" : [{\"accountName\":\"Low risk account\",\"wallets\": [{\"walletName\": \"Wallet BOND 01\", \"type\": \"BOND\"}]}]}";
		mockMvc.perform(MockMvcRequestBuilders.post("/api/openAccount")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.status().isBadRequest())
		.andExpect(MockMvcResultMatchers.jsonPath("$.entityName", Is.is("entityName is mandatory")))
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	

	@Test
	public void testValidateRequestBody_invalidWalletType() throws Exception {
		String json = "{\"entityName\" : \"HenryEntity\",\"account\" : [{\"accountName\":\"Low risk account\",\"wallets\": [{\"walletName\": \"Wallet BOND 01\", \"type\": \"AAAA\"}]}]}";
		mockMvc.perform(MockMvcRequestBuilders.post("/api/openAccount")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.status().isBadRequest())
		.andExpect(MockMvcResultMatchers.content().string(containsString("account[0].wallets[0].type")))
		.andExpect(MockMvcResultMatchers.content().string(containsString("invalid value for this field")))
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	public void testValidateRequestBody_accountIsNull() throws Exception {
		String json = "{\"entityName\" : \"HenryEntity\"}";
		mockMvc.perform(MockMvcRequestBuilders.post("/api/openAccount")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.status().isBadRequest())
		.andExpect(MockMvcResultMatchers.jsonPath("$.account", Is.is("account is mandatory")))
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	public void testValidateRequestBody_walletIsNull() throws Exception {
		String json = "{\"entityName\" : \"HenryEntity\",\"account\" : [{\"accountName\":\"Low risk account\"}]}";
		mockMvc.perform(MockMvcRequestBuilders.post("/api/openAccount")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.status().isBadRequest())
		.andExpect(MockMvcResultMatchers.content().string(containsString("account[0].wallets")))
		.andExpect(MockMvcResultMatchers.content().string(containsString("wallet is mandatory")))
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	public void testOK() throws Exception {
		String json = "{\"entityName\" : \"HenryEntity\",\"account\" : [{\"accountName\":\"Low risk account\",\"wallets\": [{\"walletName\": \"Wallet BOND 01\", \"type\": \"BOND\"}]}]}";
		mockMvc.perform(MockMvcRequestBuilders.post("/api/openAccount")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.status().isCreated())
		.andExpect(MockMvcResultMatchers.jsonPath("$.message", Is.is("Account creation request is processing in async mode!")))
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
	}
	
    
}
