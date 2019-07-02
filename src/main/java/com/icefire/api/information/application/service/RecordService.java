package com.icefire.api.information.application.service;

import com.google.common.base.Throwables;
import com.icefire.api.common.application.exception.BadValueException;
import com.icefire.api.common.application.exception.RecordNotFoundException;
import com.icefire.api.common.application.exception.UserNotFoundException;
import com.icefire.api.common.infrastructure.security.AESCipher;
import com.icefire.api.common.infrastructure.security.MyKeyGenerator;
import com.icefire.api.common.infrastructure.security.RSACipher;
import com.icefire.api.information.application.dto.RecordDTO;
import com.icefire.api.information.domain.model.Record;
import com.icefire.api.information.domain.repository.RecordRepository;
import com.icefire.api.information.rest.RecordRestController;
import com.icefire.api.user.application.service.UserService;
import com.icefire.api.user.domain.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Service
public class RecordService {

    @Autowired
    RecordRepository recordRepository;

    @Autowired
    RecordAssembler recordAssembler;

    @Autowired
    UserService userService;

    public Resources<RecordDTO> allRecords() {
        final Resources<RecordDTO> resources = new Resources<>(recordAssembler.toResources(recordRepository.findAll()));
        resources.add(linkTo(methodOn(RecordRestController.class)
                .allRecords()).withRel("records").withSelfRel().withType(HttpMethod.GET.toString()));
        return resources;
    }

    public Resources<RecordDTO> allUserRecords(Long id) {
        User user = userService.getUser(id);
        final Resources<RecordDTO> resources = new Resources<>(recordAssembler.toResources(recordRepository.findAllByUser(user)));
        resources.add(linkTo(methodOn(RecordRestController.class)
                .allUserRecords(user.getId())).withRel("records").withSelfRel().withType(HttpMethod.GET.toString()));
        return resources;
    }

    public RecordDTO getRecord(Long id) {
        User user = userService.getUser(id);
        return recordAssembler.toResource(recordRepository.findById(id).orElse(null));
    }

    public RecordDTO encrypt(String value, String username) throws UserNotFoundException {
        User user = userService.getUser(username);

        if (user == null) {
            throw new UserNotFoundException(username, Throwables.getRootCause(new Throwable("User does not exist!")));
        }

        SecretKey secretKey = key(username, user);
        byte[] iv = MyKeyGenerator.getIV(username);
        AESCipher aesCipher = new AESCipher(secretKey, iv);

        String encryptedMessage = aesCipher.getEncryptedMessage(value);

        Record record = new Record();
        record.setValue(encryptedMessage);

        user.setPublicKey(MyKeyGenerator.keyPairGenerator(username, secretKey, iv));
        userService.saveUser(user);

        record.setUser(user);

        Record recordEntity = null;
        try {
            recordEntity = recordRepository.save(record);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return recordAssembler.toResource(recordEntity);
    }

    public RecordDTO encrypt(String value, String username, Long id) throws RecordNotFoundException, UserNotFoundException {
        User user = userService.getUser(username);

        if (user == null) {
            throw new UserNotFoundException(username, Throwables.getRootCause(new Throwable("User does not exist! User may have been deleted")));
        }

        SecretKey secretKey = key(username, user);
        byte[] iv = MyKeyGenerator.getIV(username);
        AESCipher aesCipher = new AESCipher(secretKey, iv);

        String encryptedMessage = aesCipher.getEncryptedMessage(value);

        user.setPublicKey(MyKeyGenerator.keyPairGenerator(username, secretKey, iv));
        userService.saveUser(user);

        Record record = recordRepository.findById(id).orElse(null);
        if (record == null) {
            throw new RecordNotFoundException(id, Throwables.getRootCause(new Throwable("Record does not exist!")));
        }

        record.setValue(encryptedMessage);
        record.setUpdated(LocalDateTime.now());

        Record recordEntity = null;
        try {
            recordEntity = recordRepository.save(record);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return recordAssembler.toResource(recordEntity);
    }

    public RecordDTO decrypt(String value, Long id, String username) throws RecordNotFoundException, BadValueException {
        RecordDTO record = recordAssembler.toResource(recordRepository.findById(id).orElse(null));

        if (record == null) {
            throw new RecordNotFoundException(id, Throwables.getRootCause(new Throwable("Record does not exist!")));
        }

        if (!value.equalsIgnoreCase(record.getValue())) {
            throw new BadValueException();
        }

        User user = userService.getUser(username);

        AESCipher aesCipher = cipher(username, user);

        record.setValue(aesCipher.getDecryptedMessage(value));
        return record;
    }

    private AESCipher cipher(String username, User user) {
        RSACipher rsaCipher = new RSACipher(MyKeyGenerator.getPrivateKey(username));
        return new AESCipher(MyKeyGenerator.getSecretKey(rsaCipher.decryptedKey(user.getPublicKey())), MyKeyGenerator.getIV(username));
    }

    private SecretKey key(String username, User user) {
        RSACipher rsaCipher = new RSACipher(MyKeyGenerator.getPrivateKey(username));
        return MyKeyGenerator.getSecretKey(rsaCipher.decryptedKey(user.getPublicKey()));
    }
}
