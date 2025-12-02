package com.example.voituree.controllers;

import com.example.voituree.Entity.Client;
import com.example.voituree.Entity.Voiture;
import com.example.voituree.Services.ClientService;
import com.example.voituree.repositories.VoitureRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import java.util.List;

@RestController
public class VoitureController {

    @Autowired
    VoitureRepository voitureRepository;

    @Autowired
    ClientService clientService;

    @GetMapping("/voitures")
    public ResponseEntity<?> findAll() {
        return ResponseEntity.ok(voitureRepository.findAll());
    }

    @GetMapping("/voitures/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        try {
            Voiture voiture = voitureRepository.findById(id)
                    .orElseThrow(() -> new Exception("Voiture introuvable"));

            // Get client via Feign using new field clientId
            Client client = clientService.clientById(voiture.getIdClient());
            voiture.setClient(client);

            return ResponseEntity.ok(voiture);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Voiture not found with ID: " + id);
        }
    }

    @GetMapping("/voitures/client/{id}")
    public ResponseEntity<List<Voiture>> findByClient(@PathVariable Long id) {
        try {
            Client client = clientService.clientById(id);
            if (client != null) {
                // Harmonisez avec votre repo: findByIdClient(id) si le champ est id_client
                List<Voiture> voitures = voitureRepository.findByIdClient(id);
                return ResponseEntity.ok(voitures);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PostMapping("/voitures/{clientId}")
    public ResponseEntity<?> save(@PathVariable Long clientId, @RequestBody Voiture voiture) {
        try {
            Client client = clientService.clientById(clientId);

            if (client == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Client not found");
            }

            voiture.setIdClient(clientId);  // updated field name
            voiture.setClient(client);      // transient field

            Voiture saved = voitureRepository.save(voiture);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving voiture: " + e.getMessage());
        }
    }
}