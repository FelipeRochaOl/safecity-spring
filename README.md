# SafeCity - Sistema Completo de Segurança Urbana

SafeCity é um sistema completo para reportar e gerenciar incidentes de segurança urbana, composto por:

- **Aplicativo Flutter** - App móvel para usuários reportarem incidentes
- **API Spring Boot** - Backend com autenticação JWT e integração PostgreSQL
- **Dashboard Angular** - Painel administrativo para gerenciar incidentes

## 🏗️ Arquitetura do Sistema

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Flutter App   │    │  Spring Boot    │    │ Angular Dashboard│
│   (Mobile)      │◄──►│     API         │◄──►│   (Admin Web)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │   PostgreSQL    │
                       │    Database     │
                       └─────────────────┘
```

## 📱 Funcionalidades

### Aplicativo Flutter
- ✅ Tela de login e cadastro
- ✅ Mapa interativo com incidentes
- ✅ Registro de novos incidentes
- ✅ Histórico pessoal de incidentes
- ✅ Sistema de notificações
- ✅ Geolocalização automática

### API Spring Boot
- ✅ Autenticação JWT
- ✅ CRUD completo de incidentes
- ✅ Sistema de notificações
- ✅ Endpoints para dashboard administrativo
- ✅ Integração com PostgreSQL
- ✅ Validação de dados
- ✅ CORS configurado

### Dashboard Angular
- ✅ Login administrativo
- ✅ Estatísticas em tempo real
- ✅ Gráficos e visualizações
- ✅ Gerenciamento de incidentes
- ✅ Lista de usuários
- ✅ Interface responsiva

## 🚀 Como Executar

### 1. Configurar o Banco de Dados

```bash
# Iniciar PostgreSQL com Docker
docker-compose up -d postgres

# Ou instalar PostgreSQL localmente e executar:
cd docker-entrypoint-initdb.d
psql -U safecity_user -d safecity -f init.sql
```

-> Acessar o banco de dados:

- Abrir o navedor
- Digitar: http://localhost:5050
- Deve carregar o pgadmin do postgres
- Entrar usando o login:
- Email: admin@safecity.com - Senha: admin123
- Configurar novo servidor
- Host: postgres (nome do serviço do dockerfile)
- Port: 5432
- User: safecity_user
- Senha: safecity_password



### 2. Executar a API Spring Boot

```bash
cd safecity-backend

# Instalar Maven (se não tiver)
# Ubuntu/Debian: sudo apt install maven
# macOS: brew install maven

# Executar a aplicação
mvn spring-boot:run

# A API estará disponível em: http://localhost:8080
```

### 3. Executar o Dashboard Angular

```bash
cd safecity-angular-dashboard

# Instalar dependências
npm install

# Executar em modo desenvolvimento
npm run serve

# O dashboard estará disponível em: http://localhost:4200
```

## 🔧 Configuração

### Variáveis de Ambiente

#### Spring Boot (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/safecity
    username: safecity_user
    password: safecity_password

jwt:
  secret: safecitySecretKeyForJWTTokenGeneration2024
  expiration: 86400000 # 24 horas
```

#### Angular Dashboard (src/app/services/api.service.ts)
```typescript
private readonly API_URL = 'http://localhost:8080/api';
```

#### Flutter App (lib/services/api_service.dart)
```dart
static const String baseUrl = 'http://localhost:8080/api';
```

## 📊 Endpoints da API

### Autenticação
- `POST /api/auth/signin` - Login
- `POST /api/auth/signup` - Cadastro

### Incidentes
- `GET /api/incidents` - Listar todos os incidentes
- `POST /api/incidents` - Criar incidente
- `GET /api/incidents/my` - Meus incidentes
- `GET /api/incidents/near` - Incidentes próximos
- `PUT /api/incidents/{id}/status` - Atualizar status

### Notificações
- `GET /api/notifications` - Minhas notificações
- `GET /api/notifications/unread` - Não lidas
- `PUT /api/notifications/{id}/read` - Marcar como lida

### Dashboard (Admin)
- `GET /api/dashboard/stats` - Estatísticas gerais
- `GET /api/dashboard/incidents/recent` - Incidentes recentes
- `GET /api/dashboard/users` - Lista de usuários

## 🗄️ Estrutura do Banco de Dados

### Tabela: users
- `id` (BIGINT, PK)
- `name` (VARCHAR)
- `email` (VARCHAR, UNIQUE)
- `password` (VARCHAR)
- `phone` (VARCHAR)
- `role` (ENUM: USER, ADMIN)
- `enabled` (BOOLEAN)
- `created_at`, `updated_at` (TIMESTAMP)

### Tabela: incidents
- `id` (BIGINT, PK)
- `title` (VARCHAR)
- `description` (TEXT)
- `latitude`, `longitude` (DOUBLE)
- `address` (VARCHAR)
- `incident_type` (ENUM)
- `status` (ENUM: PENDING, INVESTIGATING, RESOLVED, DISMISSED)
- `user_id` (BIGINT, FK)
- `created_at`, `updated_at` (TIMESTAMP)

### Tabela: notifications
- `id` (BIGINT, PK)
- `title` (VARCHAR)
- `message` (TEXT)
- `type` (ENUM)
- `user_id` (BIGINT, FK)
- `incident_id` (BIGINT, FK, NULLABLE)
- `is_read` (BOOLEAN)
- `created_at` (TIMESTAMP)

## 🔐 Segurança

- **JWT Authentication** - Tokens seguros com expiração
- **Password Hashing** - BCrypt para senhas
- **CORS** - Configurado para desenvolvimento e produção
- **Validação** - Validação de entrada em todos os endpoints
- **Autorização** - Controle de acesso baseado em roles

## 🧪 Dados de Teste

O sistema inclui dados de teste:

**Usuário Admin:**
- Email: `admin@safecity.com`
- Senha: `password`

**Incidentes de Exemplo:**
- Furto na Praça Central
- Atividade Suspeita
- Vandalismo no Parque


### Banco de Dados
- Configure PostgreSQL em produção
- Atualize as configurações de conexão
- Execute as migrações necessárias

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature
3. Commit suas mudanças
4. Push para a branch
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo LICENSE para mais detalhes.

## 📞 Suporte

Para dúvidas ou suporte:
- Abra uma issue no GitHub
- Entre em contato com a equipe de desenvolvimento

---

**SafeCity** - Tornando as cidades mais seguras através da tecnologia! 🏙️🛡️

