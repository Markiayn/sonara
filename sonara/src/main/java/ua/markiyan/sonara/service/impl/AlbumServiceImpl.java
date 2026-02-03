package ua.markiyan.sonara.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.markiyan.sonara.dto.request.AlbumRequest;
import ua.markiyan.sonara.dto.request.ArtistAlbumRequest;
import ua.markiyan.sonara.dto.request.AlbumUpdateRequest;
import ua.markiyan.sonara.dto.response.AlbumResponse;
import ua.markiyan.sonara.entity.Album;
import ua.markiyan.sonara.entity.Artist;
import ua.markiyan.sonara.exception.NotFoundException;
import ua.markiyan.sonara.exception.ResourceAlreadyExistsException;
import ua.markiyan.sonara.mapper.AlbumMapper;
import ua.markiyan.sonara.repository.AlbumRepository;
import ua.markiyan.sonara.repository.ArtistRepository;
import ua.markiyan.sonara.service.AlbumService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlbumServiceImpl implements AlbumService {

    private final AlbumRepository albumRepo;
    private final ArtistRepository artistRepo;

    @Override
    @Transactional
    public AlbumResponse create(AlbumRequest req) {
        Artist artist = artistRepo.findById(req.artistId())
                .orElseThrow(() -> new NotFoundException("Artist %d not found".formatted(req.artistId())));

        if (albumRepo.existsByTitleIgnoreCaseAndArtist_Id(req.title(), req.artistId())) {
            throw new ResourceAlreadyExistsException("title", "Album with the same title already exists for this artist");
        }

        Album entity = AlbumMapper.toEntity(req, artist);
        Album saved = albumRepo.save(entity);
        return AlbumMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AlbumResponse get(Long id) {
        Album album = albumRepo.findWithArtistById(id)
                .orElseThrow(() -> new NotFoundException("Album %d not found".formatted(id)));
        return AlbumMapper.toResponse(album);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlbumResponse> listByArtist(Long artistId) {
        if (!artistRepo.existsById(artistId)) {
            throw new NotFoundException("Artist %d not found".formatted(artistId));
        }
        return albumRepo.findAllByArtist_Id(artistId)
                .stream()
                .map(AlbumMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AlbumResponse getUnderArtist(Long artistId, Long albumId) {
        Album album = albumRepo.findByIdAndArtist_Id(albumId, artistId)
                .orElseThrow(() -> new NotFoundException(
                        "Album %d not found for artist %d".formatted(albumId, artistId)));
        return AlbumMapper.toResponse(album);
    }

    @Override
    @Transactional
    public AlbumResponse createUnderArtist(Long artistId, ArtistAlbumRequest req) {
        Artist artist = artistRepo.findById(artistId)
                .orElseThrow(() -> new NotFoundException("Artist %d not found".formatted(artistId)));

        if (albumRepo.existsByTitleIgnoreCaseAndArtist_Id(req.title(), artistId)) {
            throw new ResourceAlreadyExistsException("title", "Album with the same title already exists for this artist");
        }

        Album album = AlbumMapper.toEntity(req, artist);
        Album saved = albumRepo.save(album);
        return AlbumMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<AlbumResponse> search(String title,
                                                                      java.time.LocalDate releaseDate,
                                                                      org.springframework.data.domain.Pageable pageable) {
        boolean hasTitle = title != null && !title.isBlank();
        boolean hasDate  = releaseDate != null;

        if (hasTitle && hasDate) {
            return albumRepo
                    .findByTitleContainingIgnoreCaseAndReleaseDate(title, releaseDate, pageable)
                    .map(AlbumMapper::toResponse);
        } else if (hasTitle) {
            return albumRepo
                    .findByTitleContainingIgnoreCase(title, pageable)
                    .map(AlbumMapper::toResponse);
        } else if (hasDate) {
            return albumRepo
                    .findByReleaseDate(releaseDate, pageable)
                    .map(AlbumMapper::toResponse);
        } else {
            return albumRepo
                    .findAll(pageable)
                    .map(AlbumMapper::toResponse);
        }
    }

    @Override
    @Transactional
    public AlbumResponse update(Long id, AlbumUpdateRequest req) {
        Album a = albumRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Album %d not found".formatted(id)));

        if (req.title() != null && !req.title().isBlank()) {
            // Додай перевірку, щоб не було дублікатів при оновленні
            if (!a.getTitle().equalsIgnoreCase(req.title()) &&
                    albumRepo.existsByTitleIgnoreCaseAndArtist_Id(req.title(), a.getArtist().getId())) {
                throw new ResourceAlreadyExistsException("title", "Another album with this title already exists");
            }
            a.setTitle(req.title());
        }

        Album saved = albumRepo.save(a);
        return AlbumMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!albumRepo.existsById(id)) throw new NotFoundException("Album %d not found".formatted(id));
        albumRepo.deleteById(id);
    }


}
