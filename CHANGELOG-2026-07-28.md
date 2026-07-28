# 2026-07-28 项目改动记录

## 一、配置安全加固：敏感信息环境变量化

### 改了什么

| 文件 | 改动 |
|------|------|
| [application.yml](dlyk-server/src/main/resources/application.yml) | `spring.datasource.password` 从明文 `root` 改为 `${DB_PASSWORD:root}`；`username`、`redis.host`、`redis.port` 同步改为环境变量引用；新增 `jwt.secret: ${JWT_SECRET:...}` |
| [JwtConfig.java](dlyk-server/src/main/java/com/bjpowernode/config/JwtConfig.java) | **新建**。Spring `@Configuration` 类，通过 `@Value("${jwt.secret}")` 读取密钥，在 `@PostConstruct` 注入到 `JWTUtils` |
| [JWTUtils.java](dlyk-server/src/main/java/com/bjpowernode/util/JWTUtils.java) | `public static final String SECRET` 改为私有静态变量 + `setSecret()` / `getSecret()` 方法，密钥不再硬编码在源码中 |
| [README.md](README.md) | 删除了数据库密码明文展示；新增「配置环境变量」章节，列出了 `DB_USERNAME`、`DB_PASSWORD`、`REDIS_HOST`、`REDIS_PORT`、`JWT_SECRET` 五个环境变量及其默认值 |

### 为什么这么改

原项目存在三处敏感信息硬编码：

1. **数据库密码明文写在 application.yml**（`password: root`）
2. **JWT 签名密钥写死在源代码中**（`public static final String SECRET = "dY8300olWQ3345;1d<3w48"`）
3. **README.md 部署说明里直接暴露了数据库账号密码**

面试官看到这些会直接判定缺乏安全意识。

### 作用

- `application.yml` 里的 `${DB_PASSWORD:root}` 语法表示**优先从环境变量读取，读不到用默认值**。本地开发什么都不用设，和生产部署前完全一致，但生产环境可以通过设环境变量来覆盖
- JWT 密钥从源码常量改为配置注入，配合环境变量 `JWT_SECRET`，生产环境可随时轮换密钥而不需要改代码
- README 中管理员登录账号（admin/123456）保留，这是初始数据不是系统配置，属于合理的默认值

---

## 二、后端输入校验：JSR-303 Bean Validation

### 改了什么

| 文件 | 改动 |
|------|------|
| [pom.xml](dlyk-server/pom.xml) | 新增 `spring-boot-starter-validation` 依赖（Spring Boot 3.x 不再默认包含） |
| 12 个 Query 类 | 在关键业务字段上添加 `@NotBlank` / `@NotNull` 注解，附带中文 `message` |
| 10 个 Controller | 所有 POST/PUT 方法的 Query 参数前加 `@Valid`，共 22 个方法签名 |
| [GlobalExceptionHandler.java](dlyk-server/src/main/java/com/bjpowernode/config/handler/GlobalExceptionHandler.java) | 新增 `MethodArgumentNotValidException` 处理器，拼接所有字段校验错误为中文返回 |

### 各 Query 类的校验规则

| Query 类 | 必填字段 |
|----------|---------|
| `UserQuery` | 登录账号、用户姓名。密码由 Service 层单独校验（创建必填，编辑可选） |
| `ClueQuery` | 负责人、姓名、手机号、称呼、意向状态、线索来源 |
| `CustomerQuery` | 线索ID、意向产品 |
| `ActivityQuery` | 负责人、活动名称、开始时间、结束时间、活动成本 |
| `ActivityRemarkQuery` | 活动ID、备注内容 |
| `ClueRemarkQuery` | 线索ID、备注内容 |
| `TranQuery` | 客户、交易金额、预计成交日期、交易阶段 |
| `ProductQuery` | 产品名称、指导价、报价、状态 |
| `DicTypeQuery` | 类型代码、类型名称 |
| `DicValueQuery` | 类型代码、字典值、排序 |
| `SystemInfoQuery` | 系统代码、系统名称 |

### 为什么这么改

之前项目**完全没有后端输入校验**——全局搜索 `@Valid`、`@NotBlank`、`@NotNull` 结果为零。这意味着绕过前端直接 POST 空 JSON 到任何接口，数据都会直接入库。项目历史中已出现过「交易新增页面什么都不填也能提交成功」的 bug。

面试官一定会问：「如果绕过前端直接调接口呢？」

### 作用

- 空数据提交被 Controller 层拦截，返回 `{"code": 500, "msg": "客户不能为空；交易金额不能为空；预计成交日期不能为空；交易阶段不能为空"}`
- 前端校验 + 后端校验形成双重保障，符合纵深防御原则
- 错误提示是中文的，对用户体验友好

---

## 三、Service 层单元测试

### 改了什么

| 文件 | 改动 |
|------|------|
| [TranServiceImplTest.java](dlyk-server/src/test/java/com/bjpowernode/service/impl/TranServiceImplTest.java) | **新建**。5 个测试用例 |
| [UserServiceImplTest.java](dlyk-server/src/test/java/com/bjpowernode/service/impl/UserServiceImplTest.java) | **新建**。4 个测试用例 |
| [JWTUtils.java](dlyk-server/src/main/java/com/bjpowernode/util/JWTUtils.java) | `setSecret()` 移除了「只能设置一次」的守卫，允许测试中重复设置（不影响生产行为） |

### 测试用例详情

**TranServiceImplTest（5 个用例）**

| 用例 | 验证点 |
|------|--------|
| `saveTran_shouldInsertTranAndHistory` | 新增交易时同时写入 `t_tran` 和 `t_tran_history` |
| `saveTran_shouldGenerateTranNo` | 交易编号自动生成，格式为 `yyyyMMddHHmmssSSS`（17位） |
| `deleteTranById_shouldDeleteHistoryAndRemarkBeforeTran` | 先删 `t_tran_history` 和 `t_tran_remark`，再删 `t_tran`，顺序不可错（外键约束） |
| `updateTran_whenStageChanged_shouldInsertHistory` | 交易阶段变更时，自动向 `t_tran_history` 写入一条阶段变更记录 |
| `updateTran_whenStageNotChanged_shouldNotInsertHistory` | 交易阶段未变时，不写入多余的历史记录 |

**UserServiceImplTest（4 个用例）**

| 用例 | 验证点 |
|------|--------|
| `saveUser_shouldEncodePassword` | 新增用户时密码被 `BCryptPasswordEncoder` 加密后入库，非明文 |
| `changePassword_withCorrectOldPwd_shouldSucceed` | 旧密码正确 → 修改成功，新密码被加密存储 |
| `changePassword_withWrongOldPwd_shouldFail` | 旧密码错误 → 返回 `false`，不执行更新 |
| `changePassword_userNotFound_shouldFail` | 用户不存在 → 返回 `false`，不执行任何密码匹配和更新 |

### 为什么这么改

之前项目只有一个空的 `DlykServerApplicationTests.contextLoads()`，**零测试覆盖**。面试官问「你怎么保证代码质量」，没有答案。

### 作用

- 覆盖了两个最核心的 Service：交易管理（含复杂阶段流转逻辑）和用户管理（含密码加密逻辑）
- 使用 Mockito 纯单元测试，不依赖数据库，执行速度快（9 个用例 < 10 秒）
- 验证了关键业务规则的正确性：外键约束删除顺序、阶段变更自动记录历史、密码加密存储

---

## 运行测试

```bash
cd dlyk-server

# 只跑这两个测试类
./mvnw test -Dtest="TranServiceImplTest,UserServiceImplTest"

# 跑全部测试
./mvnw test
```

输出示例：

```
Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```
---

## 四、校验失败 HTTP 状态码修正：200 → 400

### 改了什么

| 文件 | 改动 |
|------|------|
| [GlobalExceptionHandler.java](dlyk-server/src/main/java/com/bjpowernode/config/handler/GlobalExceptionHandler.java) | `MethodArgumentNotValidException` 处理器加 `@ResponseStatus(HttpStatus.BAD_REQUEST)`，校验失败返回 HTTP 400 |
| [httpRequest.js](dlyk-front/src/http/httpRequest.js) | 错误拦截器新增逻辑：从 `error.response.data` 提取 `msg`，通过 `ElMessage.error()` 弹窗展示 |

### 为什么这么改

之前的 `@RestControllerAdvice` 处理校验异常时返回 HTTP 200 + 业务码 500。虽然在浏览器里能正常工作（前端认 `response.data.code`），但从 HTTP 语义上，参数校验失败应该是 400（Bad Request）而不是 200。如果将来接入 API 网关或基于 HTTP 状态码做监控告警，200 的校验失败会被误统计为成功请求。

### 作用

- 校验失败链路：后端 400 + JSON `msg` → axios 进 error 拦截器 → `ElMessage.error` 弹窗
- HTTP 400 明确标识「客户端请求错误」，与 200 成功、500 服务端错误区分开来
- 日志系统 / API 网关可以根据 4xx 状态码做独立统计

---

## 五、登录接口参数校验

### 改了什么

| 文件 | 改动 |
|------|------|
| [LoginValidationFilter.java](dlyk-server/src/main/java/com/bjpowernode/config/filter/LoginValidationFilter.java) | **新建**。`OncePerRequestFilter`，拦截 `POST /api/login`，校验 `loginAct` 和 `loginPwd` 非空 |
| [SecurityConfig.java](dlyk-server/src/main/java/com/bjpowernode/config/SecurityConfig.java) | 注入 `LoginValidationFilter`，通过 `addFilterBefore(loginValidationFilter, UsernamePasswordAuthenticationFilter.class)` 放置在认证之前 |
| [UserServiceImpl.java](dlyk-server/src/main/java/com/bjpowernode/service/impl/UserServiceImpl.java) | `loadUserByUsername` 的 `UsernameNotFoundException` 信息从 `"DDDDDDDD"` 改为有意义的提示 |

### 为什么这么改

登录走的是 Spring Security 的 `UsernamePasswordAuthenticationFilter`，不是 Controller，所以 `@Valid` 对登录接口无效。之前空账号提交登录，Spring Security 内部抛出 `UsernameNotFoundException`，被 `MyAuthenticationFailureHandler` 捕获后返回 HTTP 200 + 业务码 500，状态码语义不对。

### 作用

- 空账号或空密码 → `LoginValidationFilter` 直接拦截 → HTTP 400 + `"登录账号和密码不能为空"`
- 登录流程完整状态码：参数为空 400 → 认证失败 200（业务码 500）→ 登录成功 200（业务码 200）

---

## 六、交易删除事务缺陷修复

### 改了什么

| 文件 | 改动 |
|------|------|
| [TranServiceImpl.java](dlyk-server/src/main/java/com/bjpowernode/service/impl/TranServiceImpl.java) | `deleteTranById` 在 `tTranMapper.deleteByPrimaryKey(id)` 后增加返回值检查，返回 0 时手动 `throw new RuntimeException` 触发事务回滚 |

### 为什么这么改

MyBatis 的 `deleteByPrimaryKey` 在目标记录不存在时返回 0 但不抛异常。`@Transactional` 无法感知这种非异常失败，导致前两步（删除 `t_tran_history` 和 `t_tran_remark`）的改动被提交，而第三步默默失败——子表数据丢了，主表数据还在，产生数据不一致。

### 作用

- 删除不存在或已被删除的交易时，前两步删子表的操作被回滚，保证数据一致性
- 抛出带 id 的异常信息（`"删除交易失败：交易记录不存在，id=" + id`），便于排查

---

## 涉及文件总览

| 分类 | 文件数 |
|------|--------|
| 新建 | 4（JwtConfig、LoginValidationFilter、TranServiceImplTest、UserServiceImplTest） |
| 修改 | 29（pom.xml、application.yml、12 个 Query 类、10 个 Controller、JWTUtils、GlobalExceptionHandler、SecurityConfig、TranServiceImpl、UserServiceImpl、httpRequest.js、README.md） |