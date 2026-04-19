package org.example.replayservice.controller;


import lombok.RequiredArgsConstructor;
import org.example.replayservice.dto.AccountStateResponseDto;
import org.example.replayservice.dto.ReplayAtRequestDto;
import org.example.replayservice.service.ReplayService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/v1/replay")
@RequiredArgsConstructor
public class ReplayController {

    private final ReplayService replayService;

    @PostMapping("/replay-at")
    public AccountStateResponseDto replayAt(@RequestBody ReplayAtRequestDto request) {
        return replayService.replayAt(request.getAccountId(), request.getDate());
    }
}
