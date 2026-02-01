package ua.markiyan.sonara.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.*;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.markiyan.sonara.dto.request.PlaylistRequest;
import ua.markiyan.sonara.dto.response.PlaylistResponse;
import ua.markiyan.sonara.hateoas.PlaylistModelAssembler;
import ua.markiyan.sonara.service.PlaylistService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users/{userId}/playlists")
@RequiredArgsConstructor
public class UserPlaylistController {

    private final PlaylistService playlistService;
    private final PlaylistModelAssembler assembler;
    private final PagedResourcesAssembler<PlaylistResponse> pagedAssembler;

    @GetMapping
    public CollectionModel<EntityModel<PlaylistResponse>> list(@PathVariable Long userId) {
        List<PlaylistResponse> list = playlistService.listByUser(userId);
        var models = list.stream().map(assembler::toModel).collect(Collectors.toList());
        return CollectionModel.of(models,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserPlaylistController.class).list(userId)).withSelfRel());
    }

    @PostMapping
    public ResponseEntity<EntityModel<PlaylistResponse>> create(@PathVariable Long userId, @Valid @RequestBody PlaylistRequest req) {
        PlaylistResponse created = playlistService.create(userId, req);
        return ResponseEntity.status(201).body(assembler.toModel(created));
    }
}
