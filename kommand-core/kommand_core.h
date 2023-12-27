typedef enum ErrorType {
  None,
  Io,
  Utf8,
  Unknown,
} ErrorType;

typedef enum Stdio {
  Inherit,
  Null,
  Pipe,
} Stdio;

typedef struct EnvVars {
  char **names;
  char **values;
  unsigned long long len;
} EnvVars;

typedef struct VoidResult {
  void *ok;
  char *err;
  enum ErrorType error_type;
} VoidResult;

typedef struct UnitResult {
  int ok;
  char *err;
  enum ErrorType error_type;
} UnitResult;

typedef struct IntResult {
  int ok;
  char *err;
  enum ErrorType error_type;
} IntResult;

typedef struct Output {
  int exit_code;
  char *stdout_content;
  char *stderr_content;
} Output;

/**
 * # Safety
 */
char *env_var(const char *name);

struct EnvVars env_vars(void);

void drop_env_vars(struct EnvVars env_vars);

/**
 * # Safety
 * Will drop the string
 */
void drop_string(char *string);

char *void_to_string(void *ptr);

/**
 * # Safety
 */
struct VoidResult read_line_stdout(const void *reader, unsigned long long *size);

/**
 * # Safety
 */
struct VoidResult read_all_stdout(const void *reader, unsigned long long *size);

/**
 * # Safety
 */
struct VoidResult read_line_stderr(const void *reader, unsigned long long *size);

/**
 * # Safety
 */
struct VoidResult read_all_stderr(const void *reader, unsigned long long *size);

/**
 * # Safety
 */
struct UnitResult write_line_stdin(const void *writer, const char *line);

struct UnitResult flush_stdin(const void *writer);

void drop_stderr(void *reader);

void drop_stdin(void *reader);

void drop_stdout(void *reader);

void *buffered_stdin_child(const void *child);

void *buffered_stdout_child(const void *child);

void *buffered_stderr_child(const void *child);

struct UnitResult kill_child(const void *child);

unsigned int id_child(const void *child);

struct IntResult wait_child(const void *child);

/**
 * The returned [Output] will empty
 * if convert the [Child.stdout] and [Child.stderr] to buffered
 * with [buffered_stdout_child] and [buffered_stderr_child]
 */
struct VoidResult wait_with_output_child(void *child);

void drop_child(void *child);

/**
 * # Safety
 * Will not move the [name]'s ownership
 * You must drop the command with [drop_command]
 *
 * ```rust
 * use kommand_core::ffi_util::as_cstring;
 * use kommand_core::process::{drop_command, new_command};
 * unsafe {
 *     let command = new_command(as_cstring("pwd").as_ptr());
 *     drop_command(command);
 * }
 * ```
 */
void *new_command(const char *name);

/**
 * ```rust
 * use kommand_core::ffi_util::{as_cstring, drop_string};
 * use kommand_core::process::{display_command, drop_command, new_command};
 * unsafe {
 *     let command = new_command(as_cstring("pwd").as_ptr());
 *     let display = display_command(command);
 *     drop_string(display);
 *     drop_command(command);
 * }
 * ```
 */
char *display_command(const void *command);

/**
 * ```rust
 * use kommand_core::ffi_util::{as_cstring, drop_string};
 * use kommand_core::process::{debug_command, drop_command, new_command};
 * unsafe {
 *     let command = new_command(as_cstring("pwd").as_ptr());
 *     let debug = debug_command(command);
 *     drop_string(debug);
 *     drop_command(command);
 * }
 * ```
 */
char *debug_command(const void *command);

/**
 * ```rust
 * use kommand_core::ffi_util::as_cstring;
 * use kommand_core::process::{drop_command, new_command};
 * unsafe {
 *     let command = new_command(as_cstring("pwd").as_ptr());
 *     drop_command(command);
 * }
 * ```
 */
void drop_command(void *command);

/**
 * # Safety
 * Will not take over ownership of arg
 *
 * ```rust
 * use kommand_core::ffi_util::as_cstring;
 * use kommand_core::process::{arg_command, drop_command, new_command};
 * unsafe {
 *     let command = new_command(as_cstring("ls").as_ptr());
 *     arg_command(command, as_cstring("-l").as_ptr());
 *     drop_command(command);
 * }
 * ```
 */
void arg_command(const void *command, const char *arg);

/**
 * # Safety
 * Will not take over ownership of key & value
 *
 * ```rust
 * use kommand_core::ffi_util::as_cstring;
 * use kommand_core::process::{arg_command, drop_command, env_command, new_command};
 * unsafe {
 *     let command = new_command(as_cstring("echo").as_ptr());
 *     arg_command(command, as_cstring("$KOMMAND").as_ptr());
 *     env_command(command, as_cstring("KOMMAND").as_ptr(), as_cstring("kommand").as_ptr());
 *     drop_command(command);
 * }
 * ```
 */
void env_command(const void *command, const char *key, const char *value);

/**
 * # Safety
 * Will not drop the key
 */
void remove_env_command(const void *command, const char *key);

void env_clear_command(const void *command);

/**
 * # Safety
 * Will not drop the path
 */
void current_dir_command(const void *command, const char *path);

void stdin_command(const void *command, enum Stdio stdio);

void stdout_command(const void *command, enum Stdio stdio);

void stderr_command(const void *command, enum Stdio stdio);

struct VoidResult spawn_command(const void *command);

struct VoidResult output_command(const void *command);

struct IntResult status_command(const void *command);

/**
 * [Command::get_program] returns a [OsString] which is not compatible with C.
 * unix-like: OsString is vec<u8>
 * windows: OsString is vec<u16>
 */
char *get_program_command(const void *command);

struct Output into_output(void *ptr);

void drop_output(struct Output output);
