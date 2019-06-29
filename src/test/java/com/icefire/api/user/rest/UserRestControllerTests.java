package com.icefire.api.user.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icefire.api.ApiApplication;
import com.icefire.api.user.application.dto.UserDTO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ApiApplication.class)
@WebAppConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserRestControllerTests {

    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    @Autowired
    @Qualifier("_halObjectMapper")
    ObjectMapper mapper;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void testRegisterUserSuccessful() throws Exception {

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

        //System.out.println(result.getResponse().getContentAsString());
        Assert.assertNotNull(result);
        Assert.assertEquals(201, result.getResponse().getStatus());
        Assert.assertNotNull(userDTO.getUsername());

    }

    @Test
    public void testRegisterUserEmptyUsername() throws Exception {

        String userDetails = "{\n" +
                "\"username\": \"\",\n" +
                "\"password\": \"onuche\"\n" +
                "}";

        MvcResult result = mockMvc.perform(
                post("http://localhost:8080/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userDetails))
                .andExpect(status().isBadRequest())
                .andReturn();

        //System.out.println(result.getResponse().getContentAsString());
        Assert.assertNotNull(result);
        Assert.assertEquals(400, result.getResponse().getStatus());

    }

    @Test
    public void testRegisterUserEmptyPassword() throws Exception {

        String userDetails = "{\n" +
                "\"username\": \"Jackson\",\n" +
                "\"password\": \"\"\n" +
                "}";

        MvcResult result = mockMvc.perform(
                post("http://localhost:8080/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userDetails))
                .andExpect(status().isBadRequest())
                .andReturn();

        //System.out.println(result.getResponse().getContentAsString());
        Assert.assertNotNull(result);
        Assert.assertEquals(400, result.getResponse().getStatus());

    }

    @Test
    public void testRegisterUserAlreadyExistingUser() throws Exception {

        String userDetails = "{\n" +
                "\"username\": \"onuche\", \n" +
                "\"password\": \"onuche\"\n" +
                "}";

        MvcResult result = mockMvc.perform(
                post("http://localhost:8080/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userDetails))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult result_existing = mockMvc.perform(
                post("http://localhost:8080/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userDetails))
                .andExpect(status().isBadRequest())
                .andReturn();

        UserDTO userDTO =
                mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<UserDTO>() {
                });

        //System.out.println(result.getResponse().getContentAsString());
        Assert.assertNotNull(result);
        Assert.assertNotNull(result_existing);
        Assert.assertEquals(400, result_existing.getResponse().getStatus());
        Assert.assertNotNull(userDTO.getUsername());

    }

}
