package com.springframework.spring6restmvc.services;

import com.springframework.spring6restmvc.entities.Beer;
import com.springframework.spring6restmvc.mappers.BeerMapper;
import com.springframework.spring6restmvc.model.BeerDTO;
import com.springframework.spring6restmvc.model.BeerStyle;
import com.springframework.spring6restmvc.repositories.BeerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Primary
@RequiredArgsConstructor
public class BeerServiceJPA implements BeerService {
    private final BeerRepository beerRepository;
    private final BeerMapper beerMapper;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_PAGE_SIZE = 25;

    @Override
    public Page<BeerDTO> getAllBears(String beerName, BeerStyle beerStyle, Boolean showInventory,
                                     Integer pageNumber, Integer pageSize) {

        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize);
        Page<Beer> beerPage;

        // find in a database (prefilled by bootstrap)
        if (StringUtils.hasText(beerName) && beerStyle == null) {
            beerPage = listBeersByName(beerName, pageRequest);
        } else if (!StringUtils.hasText(beerName) && beerStyle != null) {
            beerPage = listBeersByStyle(beerStyle, pageRequest);
        } else if (StringUtils.hasText(beerName) && beerStyle != null) {
            beerPage = listBeersByNameAndStyle(beerName, beerStyle, pageRequest);
        } else {
            beerPage = beerRepository.findAll(pageRequest);
        }

        if (showInventory != null && !showInventory) {
            beerPage.forEach(beer -> beer.setQuantityOnHand(null));
        }
        return beerPage.map(beerMapper::beerToBeerDto);

        // convert to DTOs
//        return beerPage.stream()
//                .map(beerMapper::beerToBeerDto)
//                .collect(Collectors.toList());
    }

    public PageRequest buildPageRequest(Integer pageNumber, Integer pageSize) {
        int queryPageNumber;
        int queryPageSize;

        if (pageNumber != null && pageNumber > 0) {
            queryPageNumber = pageNumber - 1;
        } else {
            queryPageNumber = DEFAULT_PAGE;
        }

        if (pageSize == null) {
            queryPageSize = DEFAULT_PAGE_SIZE;
        } else {
            if (pageSize > 1000) {
                queryPageSize = 1000;
            } else {
                queryPageSize = pageSize;
            }
        }

        Sort sort = Sort.by(Sort.Order.asc("beerName"));

        return PageRequest.of(queryPageNumber, queryPageSize, sort);
    }

    private Page<Beer> listBeersByNameAndStyle(String beerName, BeerStyle beerStyle, PageRequest pageRequest) {
        return beerRepository.findAllByBeerNameIsLikeIgnoreCaseAndBeerStyle("%" + beerName + "%", beerStyle, pageRequest);
    }

    private Page<Beer> listBeersByStyle(BeerStyle beerStyle, PageRequest pageRequest) {
        return beerRepository.findAllByBeerStyle(beerStyle, pageRequest);
    }

    public Page<Beer> listBeersByName(String beerName, PageRequest pageRequest){
        return beerRepository.findAllByBeerNameIsLikeIgnoreCase("%" + beerName + "%", pageRequest);
    }

    @Override
    public Optional<BeerDTO> getBeerById(UUID id) {
        return Optional.ofNullable(beerMapper.beerToBeerDto(beerRepository.findById(id)
                .orElse(null)));
    }

    // 1. beerMapper.beerDtoToBeer(beer) — converts the DTO into a Beer entity.
    //    Hibernate will generate the UUID (id) during the save operation,
    //    and the version field will be initialized to 0 upon the first save.
    // 2. beerRepository.save(...) — persists the entity to the database
    //    (an INSERT is automatically executed if the entity has no id).
    // 3. beerMapper.beerToBeerDto(...) — converts the saved entity back into a DTO
    //    and returns it to the client with the generated id and version.
    @Override
    public BeerDTO saveBeer(BeerDTO beer) {
        return beerMapper.beerToBeerDto(beerRepository.save(beerMapper.beerDtoToBeer(beer)));
    }

    // Previously, the BeerController always returned status 201, and this method returned void
    // and simply tried to find and update a beer inside its body.
    // If the record didn’t exist, nothing happened.
    //
    // Now, the controller expects a response from this method, and if it’s empty, it throws a NotFoundException.
    //
    // Accordingly, the service now returns a result:
    //   • Optional.of(updatedBeerDTO) → if the update was successful
    //   • Optional.empty() → if the record was not found
    @Override
    public Optional<BeerDTO> updateBeerById(UUID beerId, BeerDTO beer) {
        AtomicReference<Optional<BeerDTO>> atomicReference = new AtomicReference<>();

        // Since ifPresentOrElse does not return a value and only performs actions,
        // we need a way to return an Optional<BeerDTO> from inside the lambda —
        // that’s why an AtomicReference is used here.
        beerRepository.findById(beerId)
                .ifPresentOrElse(foundBeer -> {
                    foundBeer.setBeerName(beer.getBeerName());
                    foundBeer.setBeerStyle(beer.getBeerStyle());
                    foundBeer.setUpc(beer.getUpc());
                    foundBeer.setPrice(beer.getPrice());
                    foundBeer.setQuantityOnHand(beer.getQuantityOnHand());
                    atomicReference.set(Optional.of(beerMapper
                            .beerToBeerDto(beerRepository.save(foundBeer))));
                }, () -> atomicReference.set(Optional.empty()));

        return atomicReference.get();
    }

    @Override
    public Boolean deleteBeerById(UUID beerId) {
        if (beerRepository.existsById(beerId)) {
            beerRepository.deleteById(beerId);
            return true;
        }
        return false;
    }

    @Override
    public Optional<BeerDTO> patchBeerById(UUID beerId, BeerDTO beer) {
        AtomicReference<Optional<BeerDTO>> atomicReference = new AtomicReference<>();

        beerRepository.findById(beerId)
                .ifPresentOrElse(foundBeer -> {
                    if (StringUtils.hasText(beer.getBeerName())) {
                        foundBeer.setBeerName(beer.getBeerName());
                    }
                    if (beer.getBeerStyle() != null) {
                        foundBeer.setBeerStyle(beer.getBeerStyle());
                    }
                    if (StringUtils.hasText(beer.getUpc())) {
                        foundBeer.setUpc(beer.getUpc());
                    }
                    if (beer.getPrice() != null) {
                        foundBeer.setPrice(beer.getPrice());
                    }
                    if (beer.getQuantityOnHand() != null) {
                        foundBeer.setQuantityOnHand(beer.getQuantityOnHand());
                    }
                    atomicReference.set(Optional.of(beerMapper
                            .beerToBeerDto(beerRepository.save(foundBeer))));
                }, () -> atomicReference.set(Optional.empty()));

        return atomicReference.get();
    }
}
