package no.vebb.f1.mail;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MailService {

    private final MailingListRepository mailingListRepository;

    public MailService(MailingListRepository mailingListRepository) {
        this.mailingListRepository = mailingListRepository;
    }

    public void addToMailingList(UUID userId, String email) {
        mailingListRepository.save(new Mail(userId, email));
    }
}
