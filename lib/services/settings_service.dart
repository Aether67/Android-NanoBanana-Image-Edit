import 'package:shared_preferences/shared_preferences.dart';

/// Settings service for managing app preferences
class SettingsService {
  static const String _apiKeyKey = 'api_key';
  static const String _themeModeKey = 'theme_mode';

  final SharedPreferences _prefs;

  SettingsService(this._prefs);

  /// Initialize settings service
  static Future<SettingsService> init() async {
    final prefs = await SharedPreferences.getInstance();
    return SettingsService(prefs);
  }

  /// Save API key
  Future<void> saveApiKey(String apiKey) async {
    await _prefs.setString(_apiKeyKey, apiKey);
  }

  /// Get API key
  String getApiKey() {
    return _prefs.getString(_apiKeyKey) ?? '';
  }

  /// Check if API key is set
  bool hasApiKey() {
    final apiKey = getApiKey();
    return apiKey.isNotEmpty;
  }

  /// Clear API key
  Future<void> clearApiKey() async {
    await _prefs.remove(_apiKeyKey);
  }

  /// Save theme mode
  Future<void> saveThemeMode(String mode) async {
    await _prefs.setString(_themeModeKey, mode);
  }

  /// Get theme mode
  String getThemeMode() {
    return _prefs.getString(_themeModeKey) ?? 'system';
  }
}
