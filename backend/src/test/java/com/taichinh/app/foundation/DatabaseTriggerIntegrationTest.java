package com.taichinh.app.foundation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.taichinh.app.entity.User;
import com.taichinh.app.entity.Wallet;
import com.taichinh.app.enums.WalletType;
import com.taichinh.app.repository.UserRepository;
import com.taichinh.app.repository.WalletRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class DatabaseTriggerIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void updatedAtTriggerUpdatesTimestampWhenRowIsChangedDirectlyInDatabase() throws Exception {
        User user = userRepository.saveAndFlush(new User(
                "trigger" + System.nanoTime(),
                "trigger" + System.nanoTime() + "@example.com",
                "hashed-password"));

        Wallet wallet = new Wallet(user.getId(), "Trigger Wallet", WalletType.BANK);
        wallet.setBalance(new BigDecimal("10.00"));
        Wallet savedWallet = walletRepository.saveAndFlush(wallet);

        LocalDateTime beforeUpdate = queryUpdatedAt(savedWallet.getId());
        Thread.sleep(25L);

        int updatedRows = jdbcTemplate.update(
                "UPDATE wallets SET name = ? WHERE id = ?",
                "Updated by SQL",
                savedWallet.getId());
        assertEquals(1, updatedRows);

        LocalDateTime afterUpdate = queryUpdatedAt(savedWallet.getId());
        assertTrue(afterUpdate.isAfter(beforeUpdate));
    }

    private LocalDateTime queryUpdatedAt(java.util.UUID walletId) {
        return jdbcTemplate.queryForObject(
                "SELECT updated_at FROM wallets WHERE id = ?",
                (rs, rowNum) -> rs.getTimestamp("updated_at").toLocalDateTime(),
                walletId);
    }
}
