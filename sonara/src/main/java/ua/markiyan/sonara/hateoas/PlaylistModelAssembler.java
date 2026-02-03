package ua.markiyan.sonara.hateoas;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;
import ua.markiyan.sonara.controller.UserPlaylistController;
import ua.markiyan.sonara.dto.response.PlaylistResponse;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PlaylistModelAssembler implements EntityModelAssembler<PlaylistResponse> {

    @Override
    public EntityModel<PlaylistResponse> toModel(PlaylistResponse playlist) {
        EntityModel<PlaylistResponse> model = EntityModel.of(playlist);
        // Playlist endpoints are nested under user. Provide links for item management and list creation.
        model.add(WebMvcLinkBuilder.linkTo(methodOn(UserPlaylistController.class).create(playlist.userId(), null)).withRel("create"));
        model.add(WebMvcLinkBuilder.linkTo(methodOn(UserPlaylistController.class).list(playlist.userId())).withRel("userPlaylists"));
        return model;
    }
}

