import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/app_provider.dart';
import '../models/main_ui_state.dart';
import '../widgets/api_key_dialog.dart';
import '../widgets/image_picker_section.dart';
import '../widgets/style_picker_section.dart';
import '../widgets/prompt_input_section.dart';
import '../widgets/generation_display.dart';
import '../widgets/action_buttons.dart';
import 'settings_screen.dart';

class MainScreen extends StatefulWidget {
  const MainScreen({super.key});

  @override
  State<MainScreen> createState() => _MainScreenState();
}

class _MainScreenState extends State<MainScreen>
    with SingleTickerProviderStateMixin {
  late AnimationController _animationController;
  final ScrollController _scrollController = ScrollController();

  @override
  void initState() {
    super.initState();
    _animationController = AnimationController(
      duration: const Duration(milliseconds: 800),
      vsync: this,
    );
    _animationController.forward();
  }

  @override
  void dispose() {
    _animationController.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      extendBodyBehindAppBar: false,
      appBar: AppBar(
        title: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              Icons.auto_awesome_rounded,
              color: Theme.of(context).colorScheme.primary,
              size: 24,
            ),
            const SizedBox(width: 10),
            const Text('NanoBanana'),
          ],
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.settings_rounded),
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => const SettingsScreen()),
              );
            },
            tooltip: 'Settings',
          ),
        ],
        elevation: 0,
      ),
      body: Consumer<AppProvider>(
        builder: (context, provider, child) {
          final state = provider.state;

          // Show API key dialog if not set
          if (state.apiKey.isEmpty) {
            WidgetsBinding.instance.addPostFrameCallback((_) {
              _showApiKeyDialog(context);
            });
          }

          return SingleChildScrollView(
            controller: _scrollController,
            physics: const BouncingScrollPhysics(),
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                _buildSection(
                  index: 0,
                  child: ImagePickerSection(
                    selectedImages: state.selectedImages,
                    onPickImages: provider.pickImages,
                    onRemoveImage: provider.removeSelectedImage,
                    onClearImages: provider.clearSelectedImages,
                  ),
                ),
                const SizedBox(height: 12),
                _buildSection(
                  index: 1,
                  child: StylePickerSection(
                    selectedIndex: state.selectedStyleIndex,
                    onStyleSelected: provider.updateStyleIndex,
                  ),
                ),
                const SizedBox(height: 12),
                _buildSection(
                  index: 2,
                  child: PromptInputSection(
                    prompt: state.currentPrompt,
                    onPromptChanged: provider.updatePrompt,
                    enabled: state.selectedStyleIndex == 4,
                  ),
                ),
                const SizedBox(height: 20),
                _buildSection(
                  index: 3,
                  child: ActionButtons(
                    canGenerate: state.selectedImages.isNotEmpty &&
                        state.currentPrompt.isNotEmpty &&
                        state.apiKey.isNotEmpty,
                    isGenerating:
                        state.generationState is GenerationStateLoading,
                    onGenerate: () {
                      provider.generateContent(null);
                      _scrollToBottom();
                    },
                    onEnhance: () => provider.enhanceImage(),
                    canEnhance:
                        state.generationState is GenerationStateSuccess,
                  ),
                ),
                const SizedBox(height: 20),
                GenerationDisplay(
                  generationState: state.generationState,
                  enhancementState: state.enhancementState,
                  onReset: provider.resetToIdle,
                  onSaveVariant: provider.saveAsVariant,
                ),
                const SizedBox(height: 24),
              ],
            ),
          );
        },
      ),
    );
  }

  Widget _buildSection({required int index, required Widget child}) {
    return TweenAnimationBuilder<double>(
      tween: Tween(begin: 0.0, end: 1.0),
      duration: Duration(milliseconds: 400 + (index * 100)),
      curve: Curves.easeOutCubic,
      builder: (context, value, _) {
        return Transform.translate(
          offset: Offset(0, 30 * (1 - value)),
          child: Opacity(
            opacity: value,
            child: child,
          ),
        );
      },
    );
  }

  void _scrollToBottom() {
    Future.delayed(const Duration(milliseconds: 500), () {
      if (_scrollController.hasClients) {
        _scrollController.animateTo(
          _scrollController.position.maxScrollExtent + 300,
          duration: const Duration(milliseconds: 800),
          curve: Curves.easeOutCubic,
        );
      }
    });
  }

  void _showApiKeyDialog(BuildContext context) {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => const ApiKeyDialog(),
    );
  }
}
