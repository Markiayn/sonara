package ua.markiyan.sonara.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.markiyan.sonara.dto.request.AlbumTrackRequest;
import ua.markiyan.sonara.dto.request.TrackRequest;
import ua.markiyan.sonara.dto.response.TrackResponse;
import ua.markiyan.sonara.entity.Album;
import ua.markiyan.sonara.entity.Artist;
import ua.markiyan.sonara.entity.Track;
import ua.markiyan.sonara.exception.NotFoundException;
import ua.markiyan.sonara.exception.ResourceAlreadyExistsException;
import ua.markiyan.sonara.mapper.TrackMapper;
import ua.markiyan.sonara.repository.AlbumRepository;
import ua.markiyan.sonara.repository.ArtistRepository;
import ua.markiyan.sonara.repository.TrackRepository;
import ua.markiyan.sonara.service.TrackService;
import ua.markiyan.sonara.dto.request.ArtistAlbumTrackRequest;
import ua.markiyan.sonara.dto.request.TrackUpdateRequest;

import java.util.List;


@Service
@RequiredArgsConstructor
public class TrackServiceImpl implements TrackService {

    private final TrackRepository trackRepo;
    private final AlbumRepository albumRepo;
    private final ArtistRepository artistRepo;

    @Override
    @Transactional
    public TrackResponse create(TrackRequest req) {

// 1. Використовуємо твій NotFoundException замість EntityNotFoundException
        Artist artist = artistRepo.findById(req.artistId())
                .orElseThrow(() -> new NotFoundException("Artist %d not found".formatted(req.artistId())));

        if (trackRepo.existsByTitleIgnoreCaseAndArtist_Id(req.title(), artist.getId())) {
            throw new ResourceAlreadyExistsException("title", "Track with the same title already exists for this artist");
        }

        Album album = albumRepo.findById(req.albumId())
                .orElseThrow(() -> new NotFoundException("Album not found: " + req.albumId()));

        Track track = TrackMapper.toEntity(req, album, artist);
        track.setAudioUrl(generateAudioUrl(track.getAudioKey()));
        track = trackRepo.save(track);
        return TrackMapper.toResponse(track);
    }

    @Override
    @Transactional
    public TrackResponse create(Long albumId, AlbumTrackRequest req) {
        // 1) Альбом
        Album album = albumRepo.findById(albumId)
                .orElseThrow(() -> new NotFoundException("Album not found: " + albumId));

        // 2) Артист з альбому (FK)
        Artist artist = album.getArtist();
        if (artist == null) {
            throw new IllegalStateException("Album " + album.getId() + " has no artist linked");
        }

        // 3) Перевірка дубля в межах артиста
        if (trackRepo.existsByTitleIgnoreCaseAndArtist_Id(req.title(), artist.getId())) {
            throw new ResourceAlreadyExistsException("title", "Track with the same title already exists for this artist");
        }

        // 4) Збірка ентіті
        Track track = Track.builder()
                .title(req.title())
                .durationSec(req.durationSec())
                .audioKey(req.audioKey())
                .explicitFlag(Boolean.TRUE.equals(req.explicitFlag()))
                .album(album)
                .artist(artist)
                .audioUrl(generateAudioUrl(req.audioKey()))
                .build();

        // 5) Збереження + маппінг
        track = trackRepo.save(track);
        return TrackMapper.toResponse(track);
    }


    @Override
    @Transactional(readOnly = true)
    public TrackResponse get(Long id) {
        Track t = trackRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Track not found: " + id));
        return TrackMapper.toResponse(t);
    }

    // ====== ДЛЯ /api/artists/{artistId}/albums/{albumId}/tracks ======


    @Override
    @Transactional(readOnly = true)
    public List<TrackResponse> listByAlbumWithArtsit(Long artistId, Long albumId) {
        Album album = albumRepo.findById(albumId)
                .orElseThrow(() -> new NotFoundException("Album not found: " + albumId));
        if (!album.getArtist().getId().equals(artistId)) {
            throw new NotFoundException("Album %d not found for artist %d".formatted(albumId, artistId));
        }

        return trackRepo.findByAlbum_Id(albumId).stream()
                .map(TrackMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TrackResponse getOne(Long artistId, Long albumId, Long trackId) {
        Album album = albumRepo.findById(albumId)
                .orElseThrow(() -> new NotFoundException("Album not found: " + albumId));
        if (!album.getArtist().getId().equals(artistId)) {
            throw new NotFoundException("Album %d not found for artist %d".formatted(albumId, artistId));
        }

        Track t = trackRepo.findByIdAndAlbum_Id(trackId, albumId)
                .orElseThrow(() -> new NotFoundException("Track %d not found in album %d".formatted(trackId, albumId)));
        return TrackMapper.toResponse(t);
    }

    @Override
    @Transactional
    public TrackResponse createUnderAlbum(Long artistId, Long albumId, ArtistAlbumTrackRequest req) {
        var album = albumRepo.findById(albumId)
                .orElseThrow(() -> new NotFoundException("Album not found: " + albumId));
        if (!album.getArtist().getId().equals(artistId)) {
            throw new NotFoundException("Album %d not found for artist %d".formatted(albumId, artistId));
        }

        if (trackRepo.existsByTitleIgnoreCaseAndArtist_Id(req.title(), artistId)) {
            throw new ResourceAlreadyExistsException("title", "Track with the same title already exists for this artist");
        }

        var artist = album.getArtist();
        var track = ua.markiyan.sonara.mapper.TrackMapper.toEntity(req, album, artist);
        track.setAudioUrl(generateAudioUrl(track.getAudioKey()));
        track = trackRepo.save(track);
        return ua.markiyan.sonara.mapper.TrackMapper.toResponse(track);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TrackResponse> search(String title, Integer durationSec, Boolean explicitFlag, Pageable pageable) {
        boolean ht = title != null && !title.isBlank();
        boolean hd = durationSec != null;
        boolean he = explicitFlag != null;

        Page<Track> page;
        if (ht && hd && he)
            page = trackRepo.findByTitleContainingIgnoreCaseAndDurationSecAndExplicitFlag(title, durationSec, explicitFlag, pageable);
        else if (ht && hd) page = trackRepo.findByTitleContainingIgnoreCaseAndDurationSec(title, durationSec, pageable);
        else if (ht && he)
            page = trackRepo.findByTitleContainingIgnoreCaseAndExplicitFlag(title, explicitFlag, pageable);
        else if (hd && he) page = trackRepo.findByDurationSecAndExplicitFlag(durationSec, explicitFlag, pageable);
        else if (ht) page = trackRepo.findByTitleContainingIgnoreCase(title, pageable);
        else if (hd) page = trackRepo.findByDurationSec(durationSec, pageable);
        else if (he) page = trackRepo.findByExplicitFlag(explicitFlag, pageable);
        else page = trackRepo.findAll(pageable);

        return page.map(TrackMapper::toResponse);
    }

    // TrackServiceImpl.java
    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<TrackResponse> searchInAlbum(Long albumId, String title, Integer durationSec, Boolean explicitFlag, Pageable pageable) {
        boolean ht = title != null && !title.isBlank();
        boolean hd = durationSec != null;
        boolean he = explicitFlag != null;

        org.springframework.data.domain.Page<Track> page;
        if (ht && hd && he) {
            page = trackRepo.findByAlbum_IdAndTitleContainingIgnoreCaseAndDurationSecAndExplicitFlag(albumId, title, durationSec, explicitFlag, pageable);
        } else if (ht && hd) {
            page = trackRepo.findByAlbum_IdAndTitleContainingIgnoreCaseAndDurationSec(albumId, title, durationSec, pageable);
        } else if (ht && he) {
            page = trackRepo.findByAlbum_IdAndTitleContainingIgnoreCaseAndExplicitFlag(albumId, title, explicitFlag, pageable);
        } else if (hd && he) {
            page = trackRepo.findByAlbum_IdAndDurationSecAndExplicitFlag(albumId, durationSec, explicitFlag, pageable);
        } else if (ht) {
            page = trackRepo.findByAlbum_IdAndTitleContainingIgnoreCase(albumId, title, pageable);
        } else if (hd) {
            page = trackRepo.findByAlbum_IdAndDurationSec(albumId, durationSec, pageable);
        } else if (he) {
            page = trackRepo.findByAlbum_IdAndExplicitFlag(albumId, explicitFlag, pageable);
        } else {
            page = trackRepo.findByAlbum_Id(albumId, pageable);
        }

        return page.map(TrackMapper::toResponse);
    }


    @Override
    @Transactional(readOnly = true)
    public java.util.List<TrackResponse> listByAlbum(Long albumId) {
        return trackRepo.findByAlbum_Id(albumId)
                .stream()
                .map(TrackMapper::toResponse)
                .toList();
    }


    // ====== util ======
    private String generateAudioUrl(String audioKey) {
        if (audioKey == null || audioKey.isBlank()) {
            throw new IllegalArgumentException("audioKey must not be blank");
        }
        return "https://cloudflare.com/" + audioKey + ".mp3";
    }

    @Override
    @Transactional
    public TrackResponse update(Long id, TrackUpdateRequest req) {
        Track t = trackRepo.findById(id).orElseThrow(() -> new NotFoundException("Track not found: " + id));
        if (req.title() != null && !req.title().isBlank()) t.setTitle(req.title());
        if (req.durationSec() != null) t.setDurationSec(req.durationSec());
        if (req.explicitFlag() != null) t.setExplicitFlag(req.explicitFlag());
        if (req.audioKey() != null) {
            t.setAudioKey(req.audioKey());
            t.setAudioUrl(generateAudioUrl(req.audioKey()));
        }
        if (req.audioUrl() != null) t.setAudioUrl(req.audioUrl());
        Track saved = trackRepo.save(t);
        return TrackMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!trackRepo.existsById(id)) throw new NotFoundException("Track not found: " + id);
        trackRepo.deleteById(id);
    }
}
