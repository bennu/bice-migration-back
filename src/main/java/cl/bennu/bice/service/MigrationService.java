package cl.bennu.bice.service;

import cl.bennu.bice.domain.Request;
import cl.bennu.bice.domain.Response;
import cl.bennu.bice.mock.Fund;
import cl.bennu.commons.exception.AppException;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@ApplicationScoped
public class MigrationService {

    private final Logger LOGGER = Logger.getLogger(this.getClass());
    private final static List<Integer> OPERATION_DETAILS = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);

    public Response generate(Request request) throws AppException {
        this.validate(request);

        if (request.getOperationDetailsMax() == null) request.setOperationDetailsMax(1);
        if (request.getPercentageUsdCurrency() == null) request.setPercentageUsdCurrency(80);
        if (request.getPercentageOrigin() == null) request.setPercentageOrigin(95);

        final Map<Integer, String> FUNDS = new HashMap<>();

        for (int i = 0; i < request.getFundCounter(); i++) {
            Random random = new Random();

            boolean search = true;
            do {
                int fundIndex = random.nextInt(Fund.MOCK_FUNDS.size() - 1);
                String fund = Fund.MOCK_FUNDS.get(fundIndex);

                if (StringUtils.isNotBlank(fund)) {
                    search = false;
                    FUNDS.put(fundIndex, cleanFund(fund));
                }
            } while (search);
        }

        Random random = new Random();

        final StringBuilder sql = new StringBuilder();

        for (Integer index : FUNDS.keySet()) {
            String fund = FUNDS.get(index);

            String nemo = buildNemo(fund);
            String version = toRoman(random.nextInt(40));

            String nemoFinal = nemo + "-" + version;

            createFund(request, sql, fund, nemoFinal);
            int opers = random.nextInt(0, request.getOperationsMax());
            for (int i = 0; i < opers; i++) {
                createOperation(request, sql);
            }
        }

        LOGGER.debug("SQL Generado");

        Response response = new Response();
        response.setUuid(UUID.randomUUID());
        try {
            String b64 = this.base64(sql.toString(), "Back-AA[" + response.getUuid() + "].sql");
            response.setScriptZip(b64);
        } catch (Exception e) {
            LOGGER.error("Error al generar el zip", e);
            throw new AppException("Error al generar el zip");
        }

        LOGGER.debug("ZIP Generado");

        return response;
    }

    public String base64(String sql, String fileName) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try (ZipOutputStream zipOut = new ZipOutputStream(byteArrayOutputStream)) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);

            byte[] sqlBytes = sql.getBytes(StandardCharsets.UTF_8);
            zipOut.write(sqlBytes, 0, sqlBytes.length);

            zipOut.closeEntry();
        }

        byte[] zipBytes = byteArrayOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(zipBytes);
    }

    private void validate(Request request) throws AppException {
        if (request == null) throw new AppException("No hay parametros de entrada");
        if (request.getFundCounter() == null)
            throw new AppException("No se han especificado la cantidad de fondos a generar");
        if (request.getFundCounter() <= 0) throw new AppException("La cantidad de fondos a generar debe ser mayor a 0");
        if (request.getFundCounter() > 500)
            throw new AppException("La cantidad de fondos a generar debe ser menor a 500");

        if (request.getOperationsMax() == null)
            throw new AppException("No se han especificado la cantidad de operaciones a generar");
        if (request.getOperationsMax() < 0)
            throw new AppException("La cantidad de operaciones asociadas a un fondo generar debe ser positiva");
        if (request.getOperationsMax() > 300)
            throw new AppException("La cantidad de operaciones asociadas a un fondo debe ser menor a 300");

        if (request.getOperationDetailsMax() == null)
            throw new AppException("No se han especificado la cantidad detalles de operaciones a generar");
        if (request.getOperationDetailsMax() <= 0)
            throw new AppException("La cantidad de detalles de operaciones debe ser positiva");
        if (request.getOperationDetailsMax() > 10)
            throw new AppException("La cantidad de detalles de operaciones debe ser menor a 10");
    }

    private static String cleanFund(String fund) {
        String normalized = Normalizer.normalize(fund, Normalizer.Form.NFD);
        fund = normalized.replaceAll("\\p{M}+", "");

        // Reemplazo manual solo para la ñ y Ñ, que no se cubren por normalización
        fund = fund.replace("ñ", "n").replace("Ñ", "N");

        return fund;
    }

    public static String buildNemo(String texto) {
        if (texto == null || texto.isEmpty()) return "";
        StringBuilder iniciales = new StringBuilder();
        for (String palabra : texto.trim().split("\\s+")) {
            iniciales.append(palabra.charAt(0));
        }
        return iniciales.toString();
    }

    public static String toRoman(int numero) {
        String[] miles = {"", "M", "MM", "MMM", "MMMM", "MMMMM",
                "MMMMMM", "MMMMMMM", "MMMMMMMM", "MMMMMMMMM", "MMMMMMMMMM",
                "MMMMMMMMMMM", "MMMMMMMMMMMM", "MMMMMMMMMMMMM", "MMMMMMMMMMMMMM",
                "MMMMMMMMMMMMMMM", "MMMMMMMMMMMMMMMM", "MMMMMMMMMMMMMMMMM",
                "MMMMMMMMMMMMMMMMMM", "MMMMMMMMMMMMMMMMMMM", "MMMMMMMMMMMMMMMMMMMM",
                "MMMMMMMMMMMMMMMMMMMMM", "MMMMMMMMMMMMMMMMMMMMMM", "MMMMMMMMMMMMMMMMMMMMMMM",
                "MMMMMMMMMMMMMMMMMMMMMMMM", "MMMMMMMMMMMMMMMMMMMMMMMMM",
                "MMMMMMMMMMMMMMMMMMMMMMMMMM", "MMMMMMMMMMMMMMMMMMMMMMMMMMM",
                "MMMMMMMMMMMMMMMMMMMMMMMMMMMM", "MMMMMMMMMMMMMMMMMMMMMMMMMMMMM",
                "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMM", "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM",
                "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM", "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM",
                "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM", "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM",
                "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM", "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM",
                "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM", "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM",
                "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM", "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM",
                "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM", "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM",
                "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM", "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM",
                "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM"};

        String[] centenas = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
        String[] decenas = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
        String[] unidades = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};

        return miles[numero / 1000] +
                centenas[(numero % 1000) / 100] +
                decenas[(numero % 100) / 10] +
                unidades[numero % 10];
    }

    public static int getCurrency(Integer porcentage) {
        Random random = new Random();
        int value = random.nextInt(100);

        if (value < porcentage) {
            return 1;
        } else {
            return 2 + random.nextInt(4);
        }
    }

    public static int getOrigin(Integer porcentage) {
        Random random = new Random();
        int value = random.nextInt(100);

        if (value < porcentage) {
            return 1;
        } else {
            return 2;
        }
    }

    private static void createFund(Request request, StringBuilder sql, String fundName, String nemo) {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        Random random = new Random();

        int vintage = random.nextInt(2018, 2022);
        Double commitedBase = random.nextDouble(10_000_000, 100_000_000);
        Double commitedUSD = commitedBase;
        double period = random.nextDouble(1, 10);

        String feederName = null;

        Double feederSizeBase = commitedBase;
        Double feederSizeUSD = null;
        Double masterSizeBase = random.nextDouble(100_000_000, 1_000_000_000);
        Double masterSizeUSD = null;
        Date approvalDate = null;
        Double feeFeeder = null;
        Double feeInvestment = null;
        Double feeHarvest = null;
        Double baseFees = null;

        String carry = null;
        String nemoBloomberg = null;
        Double invetmentNumber = null;
        Double coverage = null;//

        // busca relaciones
        int subAssetClassId = random.nextInt(1, 3);
        int strategyId = random.nextInt(1, 3);
        int typeId = random.nextInt(1, 4);
        int regionId = random.nextInt(1, 10);
        int sizeId = random.nextInt(1, 5);
        int managerId = random.nextInt(1, 37);
        int currencyBaseId = getCurrency(request.getPercentageUsdCurrency());
        int currencyAccountingId = getCurrency(request.getPercentageUsdCurrency());
        int accountingId = random.nextInt(1, 2);
        int agfId = random.nextInt(5, 16);
        int originId = getOrigin(request.getPercentageOrigin());


        // logica para coverage
        if (coverage == null) {
            if (accountingId == 1) {
                if (currencyAccountingId == 4 || currencyAccountingId == 1) {
                    coverage = 70.0D;
                }
            } else if (accountingId == 2) {
                if (currencyAccountingId == 4 || currencyAccountingId == 1) {
                    coverage = 100.0D;
                }
            }
        }

        sql.append("insert into fund (id, nemo_pms, custody_pms, identity_pms, name, active, feeder_name, vintage, term, commitment, commitment_usd, approved, master_size, master_size_usd, feeder_size, feeder_size_usd, fee_inversion_period, fee_harvest, base_fees, carry, fee_feeder, nemo_bloomberg, type_id, size_id, accounting_id, base_currency_id, accounting_currency_id, strategy_id, sub_asset_class_id, region_id, manager_id, agf_id, investments_number, coverage, origin_id) values (");
        sql.append("nextval('seq_fund')");
        if (nemo == null) {
            sql.append(", NULL");
        } else {
            sql.append(", '").append(nemo).append("'");
        }
        sql.append(", 'DVC'");
        sql.append(", 'Custodia'");
        sql.append(", '").append(fundName).append("'");
        sql.append(", true");
        sql.append(", '").append(feederName).append("'");
        sql.append(", ").append(vintage);
        sql.append(", ").append(period);
        if (commitedBase == null) {
            sql.append(", NULL");
        } else {
            sql.append(", ").append(commitedBase.intValue());
        }
        if (commitedUSD == null) {
            sql.append(", NULL");
        } else {
            sql.append(", ").append(commitedUSD.intValue());
        }
        if (approvalDate == null) {
            sql.append(", NULL");
        } else {
            sql.append(", '").append(dateFormat.format(approvalDate)).append("'");
        }
        sql.append(", ").append(masterSizeBase);
        sql.append(", ").append(masterSizeUSD);
        sql.append(", ").append(feederSizeBase);
        sql.append(", ").append(feederSizeUSD);
        sql.append(", ").append(feeInvestment);
        sql.append(", ").append(feeHarvest);
        sql.append(", ").append(baseFees);
        if (carry == null) {
            sql.append(", NULL");
        } else {
            sql.append(", '").append(carry).append("'");
        }
        sql.append(", ").append(feeFeeder);
        if (nemoBloomberg == null) {
            sql.append(", NULL");
        } else {
            sql.append(", '").append(nemoBloomberg).append("'");
        }
        sql.append(", ").append(typeId);
        sql.append(", ").append(sizeId);
        sql.append(", ").append(accountingId);
        sql.append(", ").append(currencyBaseId);
        sql.append(", ").append(currencyAccountingId);
        sql.append(", ").append(strategyId);
        sql.append(", ").append(subAssetClassId);
        sql.append(", ").append(regionId);
        sql.append(", ").append(managerId);
        sql.append(", ").append(agfId);
        sql.append(", ").append(invetmentNumber);
        sql.append(", ").append(coverage);
        sql.append(", ").append(originId);
        sql.append(");");
    }

    public static LocalDate getDate() {
        LocalDate start = LocalDate.of(2020, 1, 1);
        LocalDate end = LocalDate.of(2025, 3, 31);

        long days = ChronoUnit.DAYS.between(start, end);
        long randomDays = ThreadLocalRandom.current().nextLong(days + 1); // incluye fin

        return start.plusDays(randomDays);
    }

    private static void createOperation(Request request, StringBuilder sql) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Random random = new Random();
        int i = 0;
        //double index = row.getCell(i++).getNumericCellValue();

        LocalDate date = getDate();
        int typeId = random.nextInt(1, 4);
        double base = random.nextDouble(1_000_000);
        if (typeId == 1 || typeId == 3) {
            base = base * -1;
        }
        double usd = base;
        double clp = base * 950;
        double quote = 1;
        double exchangeRate = 950;
        LocalDate exchangeRateDate = date;


        sql.append(StringUtils.LF);
        sql.append("INSERT INTO operation (id,transfer_pms_id,\"date\",date_pms,exchange_rate_base_clp,exchange_rate_base_usd,exchange_rate_base_accounting,exchange_rate_date,folio,quotes,total_amount_base,total_amount_base_clp,total_amount_base_usd,fund_id,type_id,state_id,error,\"operator\", locked) ");
        sql.append("values (nextval('seq_operation'),NULL,'");
        sql.append(dateFormat.format(date));
        sql.append("','");
        sql.append(dateFormat.format(date));
        sql.append("',NULL,");
        sql.append(exchangeRate);
        sql.append(",NULL,'");
        sql.append(dateFormat.format(exchangeRateDate));
        sql.append("',0,");
        sql.append(quote);
        sql.append(",");

        sql.append(base);
        sql.append(",");
        sql.append(clp);
        sql.append(",");
        sql.append(usd);
        sql.append(",");
        sql.append("currval('seq_fund')");
        sql.append(",");
        sql.append(typeId);

        sql.append(",7,NULL,NULL, false);");

        int details = random.nextInt(1, request.getOperationDetailsMax());

        Collections.shuffle(OPERATION_DETAILS);
        List<Integer> subList = OPERATION_DETAILS.subList(0, details);

        for (int j = 0; j < details; j++) {
            double baseDetail = base / details;
            double usdDetail = baseDetail;
            double clpDetail = clp / details;

            int typeDetailId = subList.get(j);

            sql.append(StringUtils.LF);
            sql.append("INSERT INTO operation_detail (id,amount_base,amount_clp,amount_usd,operation_id,origin_id) ");
            sql.append("VALUES (nextval('seq_operation_detail'),");
            sql.append(baseDetail);
            sql.append(",");
            sql.append(clpDetail);
            sql.append(",");
            sql.append(usdDetail);
            sql.append(", currval('seq_operation'),");
            sql.append(typeDetailId);

            sql.append(");");
        }

        sql.append(StringUtils.LF);
        sql.append(StringUtils.LF);
    }

}