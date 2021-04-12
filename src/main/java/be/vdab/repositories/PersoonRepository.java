package be.vdab.repositories;

import be.vdab.domain.Gezin;
import be.vdab.dto.PersoonMetPapaEnMama;
import be.vdab.exceptions.PersoonNietGevondenException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

public class PersoonRepository extends AbstractRepository{
    public void creerEenGezin(Gezin gezin) throws SQLException{
        var insertOuder="INSERT INTO personen(voornaam) VALUES (?)";
        var insertKind = "INSERT INTO personen(voornaam,papaId,mamaId) VALUES (?,?,?)";
        try (var connection = super.getConnection();
             var statementOuder = connection.prepareStatement(insertOuder, PreparedStatement.RETURN_GENERATED_KEYS)){
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);
            statementOuder.setString(1, gezin.getPapa());
            statementOuder.addBatch();
            statementOuder.setString(1, gezin.getMama());
            statementOuder.addBatch();
            statementOuder.executeBatch();
            var result = statementOuder.getGeneratedKeys();
            result.next();
            var papaId = result.getLong(1);
            result.next();
            var mamaId = result.getLong(1);
            try (var statementKind = connection.prepareStatement(insertKind)) {
                statementKind.setLong(2, papaId);
                statementKind.setLong(3, mamaId);
                for (String kind : gezin.getKinderen()) {
                    statementKind.setString(1, kind);
                    statementKind.addBatch();
                }
                statementKind.executeBatch();
            }
            connection.commit();
        }
    }

    public Optional<PersoonMetPapaEnMama> vindEenPersoonDoorId(int id) throws SQLException{
        var sql = "SELECT p1.voornaam AS voornaam, p2.voornaam AS papaVoornaam, p3.voornaam AS mamaVoornaam FROM personen p1 LEFT OUTER JOIN personen p2 ON p1.papaId = p2.id LEFT OUTER JOIN personen p3 ON p1.mamaId = p3.id WHERE p1.id = ?";
        try (var connection = super.getConnection();
             var statement = connection.prepareStatement(sql)){
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            connection.setAutoCommit(false);
            statement.setInt(1, id);
            var result = statement.executeQuery();
            return result.next() ? Optional.of(new PersoonMetPapaEnMama(result.getString("voornaam"), result.getString("papaVoornaam"), result.getString("mamaVoornaam")))
                    :Optional.empty();
        }
    }

    public void eenPersoonOverlijdt(int id) throws SQLException{
        try (var connection = super.getConnection()) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);
            var optionalVermogen = findVermogen(id, connection);
            if (optionalVermogen.isPresent()) {
                var vermogen = optionalVermogen.get();
                if (vermogen.compareTo(BigDecimal.ZERO) > 0) {
                    var aantalKinderen = findAantalKinderen(id, connection);
                    if (aantalKinderen != 0) {
                        var erfenisPerKind = vermogen.divide(BigDecimal.valueOf(aantalKinderen), 2, RoundingMode.HALF_UP);
                        verHoogVermogenMetErfenis(id, connection, erfenisPerKind);
                    }
                    zetVermogenOpNul(id, connection);
                }
                connection.commit();
                return;
            }
            connection.rollback();
            throw new PersoonNietGevondenException();
        }
    }

    private Optional<BigDecimal> findVermogen(long id, Connection connection) throws SQLException {
        try (var statement = connection.prepareStatement(
                "select vermogen from personen where id = ? for update")) {
            statement.setLong(1, id);
            var result = statement.executeQuery();
            if (result.next()) {
                return Optional.of(result.getBigDecimal("vermogen"));
            }
            return Optional.empty();
        }
    }

    private void zetVermogenOpNul(long id,Connection connection) throws SQLException {
        try (var statement = connection.prepareStatement(
                "update personen set vermogen= 0 where id = ?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    private int findAantalKinderen(long id,Connection connection) throws SQLException{
        var sql = "select count(*) as aantalKinderen from personen where papaid=? or mamaid=?";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.setLong(2, id);
            var result = statement.executeQuery();
            result.next();
            return result.getInt("aantalKinderen");
        }
    }

    private void verHoogVermogenMetErfenis(long id, Connection connection, BigDecimal erfenis) throws SQLException {
        var sql = "update personen set vermogen = vermogen + ? where papaid = ? or mamaid = ?";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, erfenis);
            statement.setLong(2, id);
            statement.setLong(3, id);
            statement.executeUpdate();
        }
    }
}
