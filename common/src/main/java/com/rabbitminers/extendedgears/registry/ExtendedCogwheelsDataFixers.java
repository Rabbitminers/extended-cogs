package com.rabbitminers.extendedgears.registry;

import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import com.rabbitminers.extendedgears.ExtendedCogwheels;
import com.rabbitminers.extendedgears.base.data.data_fixer_api.DataFixesInternals;
import net.minecraft.Util;
import net.minecraft.util.datafix.fixes.BlockRenameFix;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExtendedCogwheelsDataFixers {
    private static final BiFunction<Integer, Schema, Schema> SAME = Schema::new;
    private static final BiFunction<Integer, Schema, Schema> SAME_NAMESPACED = NamespacedSchema::new;

    public static void init() {
        ExtendedCogwheels.LOGGER.info("Registering data fixers");
        DataFixesInternals api = DataFixesInternals.get();

        DataFixerBuilder builder = new DataFixerBuilder(ExtendedCogwheels.DATA_FIXER_VERSION);
        addFixers(builder);
        api.registerFixer(ExtendedCogwheels.DATA_FIXER_VERSION, builder.build(Util.bootstrapExecutor()));
    }

    private static void addFixers(DataFixerBuilder builder) {
        builder.addSchema(0, DataFixesInternals.BASE_SCHEMA);

        String[] woodTypes = {"dark_oak", "spruce", "oak", "birch", "jungle", "acacia", "warped", "crimson", "mangrove"};

        List<String> legacyCogwheels = Arrays.stream(woodTypes)
                .flatMap(woodType -> Stream.of(woodType + "_cogwheel", "large_" + woodType + "_cogwheel"))
                .collect(Collectors.toList());

        Schema schemaV1 = builder.addSchema(1, SAME_NAMESPACED);
        builder.addFixer(
            BlockRenameFix.create(
                schemaV1,
          "Wooden Cogwheel Block Fixer",
                createRenamer(legacyCogwheels)
            )
        );
        builder.addFixer(
            BlockRenameFix.create(
                schemaV1,
          "Wooden Cogwheel Item Fixer",
                createRenamer(legacyCogwheels)
            )
        );
    }

    private static UnaryOperator<String> createRenamer(List<String> names) {
        return string -> names.contains(string) ? string.contains("large") ? "create:large_cogwheel" : "create:cogwheel" : string;
    }
}
