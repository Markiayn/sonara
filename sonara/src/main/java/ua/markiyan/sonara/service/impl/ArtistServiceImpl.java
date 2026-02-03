package ua.markiyan.sonara.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.markiyan.sonara.dto.request.ArtistRequest;
import ua.markiyan.sonara.dto.request.ArtistUpdateRequest;
import ua.markiyan.sonara.dto.response.ArtistResponse;
import ua.markiyan.sonara.entity.Artist;
import ua.markiyan.sonara.exception.NotFoundException;
import ua.markiyan.sonara.exception.ResourceAlreadyExistsException;
import ua.markiyan.sonara.mapper.ArtistMapper;
import ua.markiyan.sonara.repository.ArtistRepository;
import ua.markiyan.sonara.service.ArtistService;


@Service
@RequiredArgsConstructor
public class ArtistServiceImpl implements ArtistService {
    private final ArtistRepository repo;


    @Override
    @Transactional
    public ArtistResponse create(ArtistRequest req) {
        if (repo.existsByNameIgnoreCase(req.name())) {
            throw new ResourceAlreadyExistsException("name", "Artist with the same name already exists");
        }
        Artist entity = ArtistMapper.toEntity(req);
        Artist saved = repo.save(entity);
        return ArtistMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ArtistResponse get(Long id) {
        Artist u = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Artist %d not found".formatted(id)));
        return ArtistMapper.toResponse(u);
    }

    @Transactional(readOnly = true)
    public Page<ArtistResponse> search(String name, String country, Pageable pageable) {
        String n = (name == null) ? "" : name.trim();
        String c = (country == null) ? "" : country.trim();
        return repo
                .findByNameContainingIgnoreCaseAndCountryContainingIgnoreCase(n, c, pageable)
                .map(ArtistMapper::toResponse);
    }

    @Override
    @Transactional
    public ArtistResponse update(Long id, ArtistUpdateRequest req) {
        Artist a = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Artist %d not found".formatted(id)));

        if (req.name() != null && !req.name().isBlank()) a.setName(req.name());
        if (req.country() != null) a.setCountry(req.country());
        if (req.startYear() != null) a.setStartYear(req.startYear());
        if (req.bio() != null) a.setBio(req.bio());

        Artist saved = repo.save(a);
        return ArtistMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new NotFoundException("Artist %d not found".formatted(id));
        repo.deleteById(id);
    }
}
