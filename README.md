# ERP — API REST

API de gestion commerciale : catalogue produits, clients, fournisseurs, devis, factures, paiements, commandes fournisseur et suivi de stock. Projet de fin de formation développeur Java (Technifutur, Belgique).

L'accent a été mis sur la cohérence des données métier — stock et paiements calculés depuis un journal plutôt que stockés, montants en `BigDecimal`, transitions d'état contrôlées côté service.

---

## Stack

| | |
|---|---|
| Java | 25 |
| Spring Boot | 4.1.0 |
| Spring Security | 7.1 (JWT via jjwt 0.13) |
| Hibernate | 7.4 |
| PostgreSQL | 18 |
| Build | Maven multi-modules |

---

## Architecture

Cinq modules Maven, dépendances descendantes :

```
api  →  bll  →  dal  →  dl
 ↓       ↓
        il
```

| Module | Contenu |
|---|---|
| **`dl`** | Entités JPA et enums — le domaine |
| **`dal`** | Repositories Spring Data |
| **`bll`** | Services métier, formulaires, exceptions |
| **`api`** | Contrôleurs REST, DTO, gestion centralisée des erreurs |
| **`il`** | Technique transverse — configuration Spring Security, JWT, horloge |

Quelques partis pris :

- **Aucune entité JPA n'est exposée en REST.** Les contrôleurs échangent des DTO (`XxxRequest` / `XxxResponse`), les services des formulaires (`XxxForm`). Les conversions vivent dans les records eux-mêmes.
- **`open-in-view: false`**, avec `@EntityGraph` sur les requêtes qui en ont besoin — pas de chargement paresseux hors transaction, pas de N+1 masqué.
- **Erreurs centralisées** dans un `@RestControllerAdvice`, réponses au format `ProblemDetail` (RFC 9457).
- **Validation déclarative** via `jakarta.validation` sur les DTO d'entrée.
- **Injection par constructeur** partout.

---

## Modèle métier

### Le flux commercial

```
Devis                          Facture                    Paiements
BROUILLON → ENVOYE → ACCEPTE   BROUILLON → VALIDEE → PAYEE
                  ↘ REFUSE                ↘ ANNULEE
```

L'acceptation d'un devis crée la facture correspondante, **aux montants figés du devis** — aucun recalcul, un prix négocié reste le prix négocié même si le tarif catalogue change entre-temps.

La validation d'une facture décrémente le stock ; son annulation après validation génère des mouvements de retour.

### Le stock n'est pas stocké

Il n'existe aucun champ `quantité` sur `Product`. Le stock est la somme d'un journal de `StockMovement` en ajout seul (`ENTREE`, `SORTIE`, `AJUSTEMENT_POSITIF`, `AJUSTEMENT_NEGATIF`, `RETOUR_CLIENT`), agrégée à la lecture par une requête groupée. Une correction se fait par un mouvement inverse, jamais par une modification.

Le même raisonnement s'applique aux paiements : une facture n'a pas de champ `montant payé`, mais une collection de `Payment` agrégée à la demande. Le paiement partiel en découle naturellement.

### Références

Générées par un compteur annuel par préfixe, avec remise à zéro chaque année :

```
PRD-2026-00001   produit
DEV-2026-00001   devis
FAC-2026-00001   facture
CMD-2026-00001   commande fournisseur
```

L'incrément passe par un `INSERT ... ON CONFLICT DO UPDATE ... RETURNING` — atomique, pas de collision en concurrence.

---

## Sécurité

Authentification par jeton JWT, API sans état (`SessionCreationPolicy.STATELESS`).

`POST /auth/login` échange un couple email / mot de passe contre un jeton, à présenter ensuite dans l'en-tête `Authorization: Bearer <jeton>`. Les mots de passe sont hachés en BCrypt.

Deux rôles, `ADMIN` et `EMPLOYEE` :

| | Lecture | Écriture |
|---|---|---|
| Produits, catégories, fournisseurs | tous | `ADMIN` |
| Clients, devis, factures, commandes, stock | tous | tous |
| Utilisateurs | `ADMIN` | `ADMIN` |

Un utilisateur change son propre mot de passe via `PUT /user/change-password` : aucun identifiant n'est accepté dans la requête, il provient du jeton.

Deux garde-fous : impossible d'archiver ou de rétrograder le dernier administrateur actif ; un compte désactivé et un mot de passe erroné renvoient le même message, pour ne pas révéler quels comptes existent.

---

## Prérequis

- JDK 25
- PostgreSQL 18, avec une base créée au préalable
- Maven (le wrapper `mvnw` est fourni)

---

## Configuration

Quatre variables d'environnement sont requises — l'application refuse de démarrer sans elles.

| Variable | Exemple |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/erp_final_project` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | `votre_mot_de_passe` |
| `JWT_SECRET` | `remplacez-moi-par-32-octets-aleatoires-minimum` |

**`JWT_SECRET` doit faire au moins 32 octets** (contrainte de HmacSHA256) et ne jamais être versionné. Pour en générer un :

```powershell
$rng = New-Object System.Security.Cryptography.RNGCryptoServiceProvider
$bytes = New-Object byte[] 48
$rng.GetBytes($bytes)
[Convert]::ToBase64String($bytes)
```

```bash
openssl rand -base64 48
```

Sous IntelliJ : `Run` → `Edit Configurations…` → `Environment variables`.

---

## Lancement

```bash
./mvnw clean install
./mvnw -pl api spring-boot:run
```

L'API écoute sur `http://localhost:8080`.

> **Attention** — le schéma est en `ddl-auto: create` : la base est **recréée à chaque démarrage**. C'est un choix de développement ; un passage à Flyway est prévu.

Au premier lancement, un jeu de données est inséré : cinq catégories, neuf produits, cinq fournisseurs, deux clients et deux utilisateurs.

### Comptes de démonstration

| Email | Mot de passe | Rôle |
|---|---|---|
| `admin@admin.be` | `test123` | `ADMIN` |
| `employee@employee.be` | `test123` | `EMPLOYEE` |

Comptes de développement, recréés à chaque démarrage.

---

## Tests

Trois collections Postman dans `postman/`, 52 requêtes avec assertions :

| Collection | Couvre |
|---|---|
| `quote-flow` | Le cycle de vie d'un devis jusqu'à la facture générée, plus les cas d'erreur |
| `security` | Authentification JWT et matrice d'autorisation |
| `user` | Gestion des comptes, changement de mot de passe, règle du dernier administrateur |

Importer dans Postman, puis `Run collection`. **Redémarrer l'application avant chaque passage** : la base étant recréée, les identifiants repartent de 1.

---

## Endpoints

| Ressource | Base |
|---|---|
| Authentification | `POST /auth/login` |
| Utilisateurs | `/user` |
| Catégories | `/category` |
| Produits | `/product` |
| Clients | `/client` |
| Fournisseurs | `/supplier` |
| Devis | `/quote` |
| Factures | `/billing` |
| Commandes fournisseur | `/purchase-order` |
| Mouvements de stock | `/stock-movement` |

Les listes sont paginées (`?page=0&size=10`) et filtrables ; les réponses suivent le format `PagedModel`.
