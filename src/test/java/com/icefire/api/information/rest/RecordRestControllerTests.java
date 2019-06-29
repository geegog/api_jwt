package com.icefire.api.information.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icefire.api.ApiApplication;
import com.icefire.api.information.application.dto.RecordDTO;
import com.icefire.api.user.application.dto.UserDTO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ApiApplication.class)
@WebAppConfiguration
@DirtiesContext(classMode=DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RecordRestControllerTests {

    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    @Autowired @Qualifier("_halObjectMapper")
    ObjectMapper mapper;



    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).apply(springSecurity()).build();
    }

    @Test
    public void testUserEncryptDataAndGetAllUserDataAndCurrentDataEncrypted() throws Exception {
        String userDetails = "{\n" +
                "\"username\": \"onuche\",\n" +
                "\"password\": \"onuche\"\n" +
                "}";

        MvcResult result = mockMvc.perform(
                post("http://localhost:8080/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userDetails))
                .andExpect(status().isCreated())
                .andReturn();

        UserDTO userDTO =
                mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<UserDTO>() {
                });

        Assert.assertNotNull(result);
        Assert.assertEquals(201, result.getResponse().getStatus());
        Assert.assertNotNull(userDTO.getUsername());

        String data = "{\n" +
                "\"value\" : \"testing 33\"\n" +
                "}";

        String encryptionUrl = userDTO.getLink("encrypt").getHref();
        String allUserDataUrl = userDTO.getLink("records").getHref();

        MvcResult allUserDataResult = mockMvc.perform(
                get(allUserDataUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Basic b251Y2hlOm9udWNoZQ=="))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult encryptionResult = mockMvc.perform(
                post(encryptionUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Basic b251Y2hlOm9udWNoZQ==")
                        .content(data))
                .andExpect(status().isOk())
                .andReturn();

        RecordDTO recordDTO =
                mapper.readValue(encryptionResult.getResponse().getContentAsString(), new TypeReference<RecordDTO>() {
                });

        String encryptedDataUrl = recordDTO.getLink("self").getHref();

        MvcResult dataResult = mockMvc.perform(
                get(encryptedDataUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Basic b251Y2hlOm9udWNoZQ=="))
                .andExpect(status().isOk())
                .andReturn();

        Assert.assertNotNull(encryptionResult);
        Assert.assertEquals(200, encryptionResult.getResponse().getStatus());
        Assert.assertEquals(200, allUserDataResult.getResponse().getStatus());
        Assert.assertEquals(200, dataResult.getResponse().getStatus());
        Assert.assertNotEquals(recordDTO.getValue(), "testing 33");
        Assert.assertNotEquals(recordDTO.getValue().length(), "testing 33".length());

    }

    @Test
    public void testUserEncryptAndDecryptData() throws Exception {
        String userDetails = "{\n" +
                "\"username\": \"onuche\",\n" +
                "\"password\": \"onuche\"\n" +
                "}";

        MvcResult result = mockMvc.perform(
                post("http://localhost:8080/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userDetails))
                .andExpect(status().isCreated())
                .andReturn();

        UserDTO userDTO =
                mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<UserDTO>() {
                });

        Assert.assertNotNull(result);
        Assert.assertEquals(201, result.getResponse().getStatus());
        Assert.assertNotNull(userDTO.getUsername());

        String dataToEncrypt = "{\n" +
                "\"value\" : \"testing 33\"\n" +
                "}";

        String encryptionUrl = userDTO.getLink("encrypt").getHref();

        MvcResult encryptionResult = mockMvc.perform(
                post(encryptionUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Basic b251Y2hlOm9udWNoZQ==")
                        .content(dataToEncrypt))
                .andExpect(status().isOk())
                .andReturn();

        RecordDTO recordDTO =
                mapper.readValue(encryptionResult.getResponse().getContentAsString(), new TypeReference<RecordDTO>() {
                });

        Assert.assertNotNull(encryptionResult);
        Assert.assertEquals(200, encryptionResult.getResponse().getStatus());
        Assert.assertNotEquals(recordDTO.getValue(), "testing 33");
        Assert.assertNotEquals(recordDTO.getValue().length(), "testing 33".length());

        String dataToDecrypt = "{\n" +
                "\"value\" : \"" + recordDTO.getValue() + "\"\n" +
                "}";

        String decryptionUrl = recordDTO.getLink("decrypt").getHref();

        MvcResult decryptionResult = mockMvc.perform(
                post(decryptionUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Basic b251Y2hlOm9udWNoZQ==")
                        .content(dataToDecrypt))
                .andExpect(status().isOk())
                .andReturn();

        RecordDTO recordDecryptDTO =
                mapper.readValue(decryptionResult.getResponse().getContentAsString(), new TypeReference<RecordDTO>() {
                });

        Assert.assertNotNull(decryptionResult);
        Assert.assertEquals(200, decryptionResult.getResponse().getStatus());
        Assert.assertEquals(recordDTO.get_id(), recordDecryptDTO.get_id());
        Assert.assertEquals(recordDecryptDTO.getValue(), "testing 33");
        Assert.assertEquals(recordDecryptDTO.getValue().length(), "testing 33".length());

    }

    @Test
    public void testUserEncryptDataTwice() throws Exception {
        String userDetails = "{\n" +
                "\"username\": \"onuche\",\n" +
                "\"password\": \"onuche\"\n" +
                "}";

        MvcResult result = mockMvc.perform(
                post("http://localhost:8080/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userDetails))
                .andExpect(status().isCreated())
                .andReturn();

        UserDTO userDTO =
                mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<UserDTO>() {
                });

        Assert.assertNotNull(result);
        Assert.assertEquals(201, result.getResponse().getStatus());
        Assert.assertNotNull(userDTO.getUsername());

        String dataToEncrypt = "{\n" +
                "\"value\" : \"testing 33\"\n" +
                "}";

        String encryptionUrl = userDTO.getLink("encrypt").getHref();

        MvcResult encryptionResult = mockMvc.perform(
                post(encryptionUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Basic b251Y2hlOm9udWNoZQ==")
                        .content(dataToEncrypt))
                .andExpect(status().isOk())
                .andReturn();

        RecordDTO recordDTO =
                mapper.readValue(encryptionResult.getResponse().getContentAsString(), new TypeReference<RecordDTO>() {
                });

        Assert.assertNotNull(encryptionResult);
        Assert.assertEquals(200, encryptionResult.getResponse().getStatus());
        Assert.assertNotEquals(recordDTO.getValue(), "testing 33");
        Assert.assertNotEquals(recordDTO.getValue().length(), "testing 33".length());

        String dataToReEncrypt = "{\n" +
                "\"value\" : \"" + recordDTO.getValue() + "\"\n" +
                "}";

        String encryptionUpdateUrl = recordDTO.getLink("encrypt_update").getHref();

        MvcResult encryptionUpdateResult = mockMvc.perform(
                post(encryptionUpdateUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Basic b251Y2hlOm9udWNoZQ==")
                        .content(dataToReEncrypt))
                .andExpect(status().isOk())
                .andReturn();

        RecordDTO recordReEncryptDTO =
                mapper.readValue(encryptionUpdateResult.getResponse().getContentAsString(), new TypeReference<RecordDTO>() {
                });

        Assert.assertNotNull(encryptionUpdateResult);
        Assert.assertEquals(200, encryptionUpdateResult.getResponse().getStatus());
        Assert.assertEquals(recordDTO.get_id(), recordReEncryptDTO.get_id());
        Assert.assertNotEquals(recordReEncryptDTO.getValue(), recordDTO.getValue());
        Assert.assertNotEquals(recordReEncryptDTO.getValue().length(), recordDTO.getValue().length());

    }

    @Test
    public void testEmptyUserEncryptData() throws Exception {
        String userDetails = "{\n" +
                "\"username\": \"onuche\",\n" +
                "\"password\": \"onuche\"\n" +
                "}";

        MvcResult result = mockMvc.perform(
                post("http://localhost:8080/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userDetails))
                .andExpect(status().isCreated())
                .andReturn();

        UserDTO userDTO =
                mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<UserDTO>() {
                });

        Assert.assertNotNull(result);
        Assert.assertEquals(201, result.getResponse().getStatus());
        Assert.assertNotNull(userDTO.getUsername());

        String dataToEncrypt = "{\n" +
                "\"value\" : \"\"\n" +
                "}";

        String encryptionUrl = userDTO.getLink("encrypt").getHref();

        MvcResult encryptionResult = mockMvc.perform(
                post(encryptionUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Basic b251Y2hlOm9udWNoZQ==")
                        .content(dataToEncrypt))
                .andExpect(status().isBadRequest())
                .andReturn();

        Assert.assertNotNull(encryptionResult);
        Assert.assertEquals(400, encryptionResult.getResponse().getStatus());

    }

    @Test
    public void testEmptyUserDecryptData() throws Exception {
        String userDetails = "{\n" +
                "\"username\": \"onuche\",\n" +
                "\"password\": \"onuche\"\n" +
                "}";

        MvcResult result = mockMvc.perform(
                post("http://localhost:8080/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userDetails))
                .andExpect(status().isCreated())
                .andReturn();

        UserDTO userDTO =
                mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<UserDTO>() {
                });

        Assert.assertNotNull(result);
        Assert.assertEquals(201, result.getResponse().getStatus());
        Assert.assertNotNull(userDTO.getUsername());

        String dataToEncrypt = "{\n" +
                "\"value\" : \"testing 33\"\n" +
                "}";

        String encryptionUrl = userDTO.getLink("encrypt").getHref();

        MvcResult encryptionResult = mockMvc.perform(
                post(encryptionUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Basic b251Y2hlOm9udWNoZQ==")
                        .content(dataToEncrypt))
                .andExpect(status().isOk())
                .andReturn();

        RecordDTO recordDTO =
                mapper.readValue(encryptionResult.getResponse().getContentAsString(), new TypeReference<RecordDTO>() {
                });

        Assert.assertNotNull(encryptionResult);
        Assert.assertEquals(200, encryptionResult.getResponse().getStatus());
        Assert.assertNotEquals(recordDTO.getValue(), "testing 33");
        Assert.assertNotEquals(recordDTO.getValue().length(), "testing 33".length());

        String dataToDecrypt = "{\n" +
                "\"value\" : \"\"\n" +
                "}";

        String decryptionUpdateUrl = recordDTO.getLink("decrypt").getHref();

        MvcResult decryptionUpdateResult = mockMvc.perform(
                post(decryptionUpdateUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Basic b251Y2hlOm9udWNoZQ==")
                        .content(dataToDecrypt))
                .andExpect(status().isBadRequest())
                .andReturn();

        Assert.assertNotNull(decryptionUpdateResult);
        Assert.assertEquals(400, decryptionUpdateResult.getResponse().getStatus());

    }

    @Test
    public void testEmptyUserDataOnSecondEncryption() throws Exception {
        String userDetails = "{\n" +
                "\"username\": \"onuche\",\n" +
                "\"password\": \"onuche\"\n" +
                "}";

        MvcResult result = mockMvc.perform(
                post("http://localhost:8080/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userDetails))
                .andExpect(status().isCreated())
                .andReturn();

        UserDTO userDTO =
                mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<UserDTO>() {
                });

        Assert.assertNotNull(result);
        Assert.assertEquals(201, result.getResponse().getStatus());
        Assert.assertNotNull(userDTO.getUsername());

        String dataToEncrypt = "{\n" +
                "\"value\" : \"testing 33\"\n" +
                "}";

        String encryptionUrl = userDTO.getLink("encrypt").getHref();

        MvcResult encryptionResult = mockMvc.perform(
                post(encryptionUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Basic b251Y2hlOm9udWNoZQ==")
                        .content(dataToEncrypt))
                .andExpect(status().isOk())
                .andReturn();

        RecordDTO recordDTO =
                mapper.readValue(encryptionResult.getResponse().getContentAsString(), new TypeReference<RecordDTO>() {
                });

        Assert.assertNotNull(encryptionResult);
        Assert.assertEquals(200, encryptionResult.getResponse().getStatus());
        Assert.assertNotEquals(recordDTO.getValue(), "testing 33");
        Assert.assertNotEquals(recordDTO.getValue().length(), "testing 33".length());

        String dataToReEncrypt = "{\n" +
                "\"value\" : \"\"\n" +
                "}";

        String encryptionUpdateUrl = recordDTO.getLink("encrypt_update").getHref();

        MvcResult encryptionUpdateResult = mockMvc.perform(
                post(encryptionUpdateUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Basic b251Y2hlOm9udWNoZQ==")
                        .content(dataToReEncrypt))
                .andExpect(status().isBadRequest())
                .andReturn();

        Assert.assertNotNull(encryptionUpdateResult);
        Assert.assertEquals(400, encryptionUpdateResult.getResponse().getStatus());

    }

    @Test
    public void testEncryptDataWithNonExistingUser() throws Exception {

        String dataToEncrypt = "{\n" +
                "\"value\" : \"hi\"\n" +
                "}";

        MvcResult encryptionResult = mockMvc.perform(
                post("http://localhost:8080/api/data/encrypt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Basic d5y51Y2dfOm9dfWNoZQ==")
                        .content(dataToEncrypt))
                .andExpect(status().isUnauthorized())
                .andReturn();

        Assert.assertNotNull(encryptionResult);
        Assert.assertEquals(401, encryptionResult.getResponse().getStatus());

    }

    @Test
    public void testEncryptDataUpdateWithWrongId() throws Exception {

        String userDetails = "{\n" +
                "\"username\": \"onuche\",\n" +
                "\"password\": \"onuche\"\n" +
                "}";

        MvcResult result = mockMvc.perform(
                post("http://localhost:8080/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userDetails))
                .andExpect(status().isCreated())
                .andReturn();

        UserDTO userDTO =
                mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<UserDTO>() {
                });

        Assert.assertNotNull(result);
        Assert.assertEquals(201, result.getResponse().getStatus());
        Assert.assertNotNull(userDTO.getUsername());

        String dataToEncrypt = "{\n" +
                "\"value\" : \"hey\"\n" +
                "}";

        MvcResult encryptionResult = mockMvc.perform(
                post("http://localhost:8080/api/data/22/encrypt_update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Basic b251Y2hlOm9udWNoZQ==")
                        .content(dataToEncrypt))
                .andExpect(status().isNotFound())
                .andReturn();

        Assert.assertNotNull(encryptionResult);
        Assert.assertEquals(404, encryptionResult.getResponse().getStatus());

    }

    @Test
    public void testDecryptDataWithWrongId() throws Exception {

        String userDetails = "{\n" +
                "\"username\": \"onuche\",\n" +
                "\"password\": \"onuche\"\n" +
                "}";

        MvcResult result = mockMvc.perform(
                post("http://localhost:8080/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userDetails))
                .andExpect(status().isCreated())
                .andReturn();

        UserDTO userDTO =
                mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<UserDTO>() {
                });

        Assert.assertNotNull(result);
        Assert.assertEquals(201, result.getResponse().getStatus());
        Assert.assertNotNull(userDTO.getUsername());

        String dataToDecrypt = "{\n" +
                "\"value\" : \"wabSn2DbG3NeD4VyKlIo7q8ffBODe0P0WQioTCmQB2pJ3SDLoq4HymIDJ1wM4LvnFaGcajqrVUWsBlKgN5oSoA1PFyFv7imnxlTddpNrE2qzHJnrSC+geJOX7ufOiEblRpZ4keWzM/lqwP+lCWhfwBaACnEFQulLPjc4nhpA81CihJUToID9awevOdc1cTIjXNYsM+FdXOe7WdWJKxgLCX5i6qWX50FmSCStE4QX5ixfoqUG7FlXn85qNI4YAE/qgPE+2+7cEH4rG1AsnAVu05dXdkEzTvgndrkG9G18pVzd/2hut0QD2ZP3ZSWaZuQVUFjKAjex6o+iPdWdMPMp3A==\"\n" +
                "}";

        MvcResult decryptionResult = mockMvc.perform(
                post("http://localhost:8080/api/data/22/decrypt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Basic b251Y2hlOm9udWNoZQ==")
                        .content(dataToDecrypt))
                .andExpect(status().isNotFound())
                .andReturn();

        Assert.assertNotNull(decryptionResult);
        Assert.assertEquals(404, decryptionResult.getResponse().getStatus());

    }

    @Test
    public void testDecryptDataWithInvalidData() throws Exception {

        String userDetails = "{\n" +
                "\"username\": \"onuche\",\n" +
                "\"password\": \"onuche\"\n" +
                "}";

        MvcResult result = mockMvc.perform(
                post("http://localhost:8080/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userDetails))
                .andExpect(status().isCreated())
                .andReturn();

        UserDTO userDTO =
                mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<UserDTO>() {
                });

        Assert.assertNotNull(result);
        Assert.assertEquals(201, result.getResponse().getStatus());
        Assert.assertNotNull(userDTO.getUsername());

        String dataToEncrypt = "{\n" +
                "\"value\" : \"testing 33\"\n" +
                "}";

        String encryptionUrl = userDTO.getLink("encrypt").getHref();

        MvcResult encryptionResult = mockMvc.perform(
                post(encryptionUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Basic b251Y2hlOm9udWNoZQ==")
                        .content(dataToEncrypt))
                .andExpect(status().isOk())
                .andReturn();

        RecordDTO recordDTO =
                mapper.readValue(encryptionResult.getResponse().getContentAsString(), new TypeReference<RecordDTO>() {
                });

        Assert.assertNotNull(encryptionResult);
        Assert.assertEquals(200, encryptionResult.getResponse().getStatus());
        Assert.assertNotEquals(recordDTO.getValue(), "testing 33");
        Assert.assertNotEquals(recordDTO.getValue().length(), "testing 33".length());

        String dataToDecrypt = "{\n" +
                "\"value\" : \"wabSn2DbG3NeD4VyKlIo7q8ffBODe0P0WQioTCmQB2pJ3SDLoq4HymIDJ1wM4LvnFaGcajqrVUWsBlKgN5oSoA1PFyFv7imnxlTddpNrE2qzHJnrSC+geJOX7ufOiEblRpZ4keWzM/lqwP+lCWhfwBaACnEFQulLPjc4nhpA81CihJUToID9awevOdc1cTIjXNYsM+FdXOe7WdWJKxgLCX5i6qWX50FmSCStE4QX5ixfoqUG7FlXn85qNI4YAE/qgPE+2+7cEH4rG1AsnAVu05dXdkEzTvgndrkG9G18pVzd/2hut0QD2ZP3ZSWaZuQVUFjKAjex6o+iPdWdMPMp3A==\"\n" +
                "}";

        MvcResult decryptionResult = mockMvc.perform(
                post(recordDTO.getLink("decrypt").getHref())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Basic b251Y2hlOm9udWNoZQ==")
                        .content(dataToDecrypt))
                .andExpect(status().isBadRequest())
                .andReturn();

        Assert.assertNotNull(decryptionResult);
        Assert.assertEquals(400, decryptionResult.getResponse().getStatus());

    }

    @Test
    public void testGetAllEncryptedData() throws Exception {

        String userDetails = "{\n" +
                "\"username\": \"onuche\",\n" +
                "\"password\": \"onuche\"\n" +
                "}";

        MvcResult result = mockMvc.perform(
                post("http://localhost:8080/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userDetails))
                .andExpect(status().isCreated())
                .andReturn();

        UserDTO userDTO =
                mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<UserDTO>() {
                });

        Assert.assertNotNull(result);
        Assert.assertEquals(201, result.getResponse().getStatus());
        Assert.assertNotNull(userDTO.getUsername());

        MvcResult allDataResult = mockMvc.perform(
                get("http://localhost:8080/api/data/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Basic b251Y2hlOm9udWNoZQ=="))
                .andExpect(status().isOk())
                .andReturn();

        Assert.assertEquals(200, allDataResult.getResponse().getStatus());

    }

}
