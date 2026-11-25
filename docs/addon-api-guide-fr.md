# API Addon de Rustling Spots

Ce guide cible Rustling Spots 6.1, Minecraft 1.21.1 et Java 21. L'API est commune à Fabric et NeoForge et doit être appelée côté serveur logique.

## Créer et gérer des spots

Définissez un spot personnalisé dans un datapack, puis créez-le à la position de bloc souhaitée. La position doit contenir de l'air, ou une source d'eau pour une famille visuelle `underwater`; son chunk doit déjà être chargé et le bloc de sol (ou le bloc d'eau sous-marin) doit correspondre à la définition. La création respecte aussi le réglage d'activation de Rustling Spots, les contrôles de compatibilité, la limite globale et la distance minimale configurée. Elle ne demande jamais le chargement d'un chunk.

```java
ResourceLocation definitionId = ResourceLocation.fromNamespaceAndPath("exampleaddon", "festival");
ResourceLocation sourceId = ResourceLocation.fromNamespaceAndPath("exampleaddon", "spring_festival");

Optional<RustlingSpot> created = RustlingSpotAddonApi.createSpot(
        serverLevel,
        spotPosition,
        definitionId,
        20 * 60, // 60 secondes
        sourceId
);

Optional<RustlingSpot> current = RustlingSpotAddonApi.getSpot(serverLevel, spotId);
Collection<RustlingSpot> inDimension = RustlingSpotAddonApi.getSpots(serverLevel);
boolean removed = RustlingSpotAddonApi.removeSpot(serverLevel, spotId);
```

`createSpot` renvoie un `Optional` vide si la définition est absente, si l'appel ne vient pas du thread serveur, si la position est invalide ou non chargée, si un autre spot est trop proche ou si la limite globale est atteinte. `lifetimeTicks` doit être positif. Passez `null` pour `sourceId` si le spot n'appartient pas à un événement d'addon particulier. Un spot créé utilise les pools Pokémon et loot, les règles shiny, les visuels, les particules et les sons de sa définition.

La définition peut être inspectée avant la création :

```java
Optional<CustomSpotDefinition> definition = RustlingSpotAddonApi.getCustomSpotDefinition(definitionId);
```

Les spots actifs exposent leur source via `spot.getSourceId()` et leur durée personnalisée via `spot.getLifetimeTicks()`; zéro signifie que la durée normale configurée sur le serveur s'applique.

## Événements de cycle de vie

```java
RustlingSpotAddonApi.registerLifecycleCallback(event -> {
    if (event.spot().getSourceId().filter(sourceId::equals).isEmpty()) {
        return;
    }

    switch (event.type()) {
        case CREATED -> onCreated(event.spot());
        case INTERACTED -> onInteracted(event.player().orElseThrow(), event.spot());
        case EXPIRED -> onExpired(event.spot());
        case REMOVED -> onRemoved(event.spot(), event.removalReason());
    }
});
```

Les callbacks observent tous les spots Rustling Spots du serveur : filtrez par `sourceId` ou identifiant de définition si votre addon ne gère qu'une partie d'entre eux. Ils s'exécutent sur le thread serveur, dans l'ordre d'inscription. Une exception est journalisée sans interrompre les autres listeners. L'API refuse de supprimer le spot pendant l'exécution de son callback de cycle de vie ou d'interaction; utilisez le résultat du callback d'interaction pour consommer ou modifier son traitement. L'expiration est suivie d'un événement de suppression avec la cause `EXPIRED`; une interaction produit aussi un événement de suppression avec la cause `INTERACTION`. `unregisterLifecycleCallback` permet de retirer un listener.

Le hook d'interaction de la v6.0 reste compatible à la compilation :

```java
RustlingSpotAddonApi.registerInteractionCallback(event -> {
    // PASS conserve la résolution normale des Pokémon et du loot.
    return RustlingSpotInteractionResult.PASS;
});
```

Les callbacks d'interaction s'exécutent toujours après validation de l'interaction et avant la résolution normale des récompenses. Le premier résultat différent de `PASS` est appliqué.

## Définitions datapack

Le champ optionnel `spawn_mode` accepte `natural` ou `external_only`. En son absence, la valeur est `natural` : les datapacks existants continuent donc de participer à la sélection naturelle. Les définitions `external_only` sont exclues de la sélection naturelle et de la commande admin; seul un addon peut les créer via l'API.

```json
{
  "format_version": 1,
  "id": "exampleaddon:festival",
  "display_name": "Festival Rustle",
  "spawn_mode": "external_only",
  "visual_family": "leaves",
  "ambient_sound": "minecraft:block.amethyst_block.chime",
  "priority": 0,
  "weight": 1,
  "dimensions": ["minecraft:overworld"],
  "biomes": ["#minecraft:is_forest"],
  "blocks": ["#minecraft:leaves"],
  "pokemon_family": "grass",
  "loot_family": "grass",
  "particles": [{"type": "minecraft:happy_villager", "weight": 1}]
}
```

`visual_family` sélectionne un renderer existant. `particles` réutilise le système existant de particules pondérées. `ambient_sound` est un identifiant facultatif d'événement sonore enregistré et disponible côté client et serveur; s'il est absent ou indisponible, le son existant de la famille visuelle est utilisé. Un addon qui ajoute un son doit enregistrer son événement et fournir ses ressources sonores côté client.

Voir le [datapack d'exemple external-only](../example_datapacks/external_only_spot_pack) et le [guide des définitions personnalisées](./custom-spot-definitions-guide.md).

## OS Cobblemon Library

OSCL 1.7.0 est publié pour les deux loaders. Sa plage Cobblemon documentée est `1.8.2` à `<1.8.5`; l'exiger relèverait donc le minimum Cobblemon actuel de Rustling Spots 1.8.0 et exclurait les versions 1.8.0 et 1.8.1.

La nouvelle API `SpawnModifiers` modifie le poids de sélection des détails de spawn Cobblemon déjà éligibles selon les règles natives. Les rencontres Rustling Spots sont créées séparément après l'interaction avec un spot; elles ne passent pas par la sélection naturelle des détails Cobblemon. Un spawn modifier n'améliorerait pas ce flux et pourrait modifier l'équilibrage des rencontres.

`PokemonCreation` et `PokemonSpeciesResolver` d'OSCL recouvrent certaines parties de `PokemonSpawnResolver`. Rustling Spots valide aussi les espèces et formes configurées, préserve les namespaces des addons, désactive le spawn cap normal et place/oriente l'entité sur le spot. Remplacer ce chemin ferait perdre ces comportements ou demanderait des branches de fallback par loader/version pour Cobblemon 1.8.0/1.8.1. Cette dépendance et cette complexité supplémentaires n'apportent pas assez de bénéfices en v6.1 : aucune dépendance OSCL n'est ajoutée et la compatibilité Cobblemon 1.8.0 est préservée.

Les addons qui choisissent d'utiliser OSCL indépendamment peuvent consommer ces fichiers Curse Maven : `curse.maven:os-cobblemon-library-1698981:9803831` (Fabric) et `curse.maven:os-cobblemon-library-1698981:9803832` (NeoForge).

Références : [documentation OSCL v1.7.0](https://levelsfr.github.io/OS-Cobblemon-Library/) · [fichier CurseForge Fabric](https://www.curseforge.com/minecraft/mc-mods/os-cobblemon-library/files/9803831) · [fichier CurseForge NeoForge](https://www.curseforge.com/minecraft/mc-mods/os-cobblemon-library/files/9803832)
