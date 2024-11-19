package org.example.ranking.writer;

import org.example.common.common.log.LogExecution;
import org.example.common.ranking.repository.RankingRepository;
import lombok.RequiredArgsConstructor;
import org.example.common.ranking.entity.Ranking;
import org.example.common.ranking.repository.RankingRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

@RequiredArgsConstructor
public class ListRankingWriter implements ItemWriter<List<Ranking>> {

    private final RankingRepository rankingRepository;

    @Override
    @LogExecution
    public void write(Chunk<? extends List<Ranking>> items) throws Exception {
        for (List<Ranking> rankings : items) {
            rankingRepository.saveAll(rankings); // Ranking 저장
        }
    }
}
