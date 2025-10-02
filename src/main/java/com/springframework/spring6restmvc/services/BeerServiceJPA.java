package com.springframework.spring6restmvc.services;

import com.springframework.spring6restmvc.mappers.BeerMapper;
import com.springframework.spring6restmvc.model.BeerDTO;
import com.springframework.spring6restmvc.repositories.BeerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Primary
@RequiredArgsConstructor
public class BeerServiceJPA implements BeerService {
    private final BeerRepository beerRepository;
    private final BeerMapper beerMapper;

    @Override
    public List<BeerDTO> listBeers() {
        return beerRepository.findAll()
                .stream()
                .map(beerMapper::beerToBeerDto)
                .collect(Collectors.toList());
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
    public BeerDTO saveNewBeer(BeerDTO beer) {
        return beerMapper.beerToBeerDto(beerRepository.save(beerMapper.beerDtoToBeer(beer)));
    }

    @Override
    public void updateBeerById(UUID beerId, BeerDTO beer) {
        beerRepository.findById(beerId)
                .ifPresent(foundBeer -> {
                    foundBeer.setBeerName(beer.getBeerName());
                    foundBeer.setBeerStyle(beer.getBeerStyle());
                    foundBeer.setUpc(beer.getUpc());
                    foundBeer.setPrice(beer.getPrice());
                    beerRepository.save(foundBeer);
                });
    }

    @Override
    public void deleteById(UUID beerId) {

    }

    @Override
    public void patchBeerById(UUID beerId, BeerDTO beer) {

    }
}
