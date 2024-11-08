package org.example.ranking.writer;

import org.example.common.ranking.repository.RankingRepository;
import org.example.common.ranking.entity.Ranking;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ListRankingWriter implements ItemWriter<List<Ranking>> {

    private final RankingRepository rankingRepository;

    @Autowired
    public ListRankingWriter(RankingRepository rankingRepository) {
        this.rankingRepository = rankingRepository;
    }

    @Override
    public void write(Chunk<? extends List<Ranking>> items) throws Exception {
        for (List<Ranking> rankings : items) {
            rankingRepository.saveAll(rankings); // Ranking 저장
        }
    }
}
