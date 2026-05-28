package app;

import java.util.prefs.Preferences;

import remote.CalculatorClient;
import repository.CategoryRepository;
import repository.ProfileRepository;
import repository.RecurringExpenseRepository;
import repository.TagRepository;
import repository.TransactionRepository;
import service.AnalyticsService;
import service.CategoryService;
import service.ProfileService;
import service.RecurringExpenseService;
import service.TagService;
import service.TransactionService;

public class AppContext {
    private static final String PREF_LAST_PROFILE_ID = "lastProfileId";
    private static final Preferences PREFS = Preferences.userNodeForPackage(AppContext.class);

    private final ProfileRepository profileRepository = new ProfileRepository();
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final TransactionRepository transactionRepository = new TransactionRepository();
    private final TagRepository tagRepository = new TagRepository();
    private final RecurringExpenseRepository recurringExpenseRepository = new RecurringExpenseRepository();

    private final CalculatorClient calculatorClient = new CalculatorClient();
    private final ProfileService profileService = new ProfileService(profileRepository);
    private final CategoryService categoryService = new CategoryService(categoryRepository);
    private final TransactionService transactionService = new TransactionService(transactionRepository, profileService, categoryService);
    private final TagService tagService = new TagService(tagRepository);

    {
        // Wire TagService into TransactionService after both are constructed
        transactionService.setTagService(tagService);
    }
    private final RecurringExpenseService recurringExpenseService = new RecurringExpenseService(recurringExpenseRepository);
    private final AnalyticsService analyticsService = new AnalyticsService();

    public CalculatorClient getCalculatorClient() { return calculatorClient; }
    public ProfileService getProfileService() { return profileService; }
    public CategoryService getCategoryService() { return categoryService; }
    public TransactionService getTransactionService() { return transactionService; }
    public TagService getTagService() { return tagService; }
    public RecurringExpenseService getRecurringExpenseService() { return recurringExpenseService; }
    public AnalyticsService getAnalyticsService() { return analyticsService; }

    public void saveLastProfile() {
        profileService.getActiveProfile()
                .ifPresentOrElse(
                        p -> PREFS.putInt(PREF_LAST_PROFILE_ID, p.getId()),
                        () -> PREFS.remove(PREF_LAST_PROFILE_ID));
    }

    public void restoreLastProfile() {
        int savedId = PREFS.getInt(PREF_LAST_PROFILE_ID, -1);
        if (savedId != -1) {
            try {
                profileService.findById(savedId).ifPresent(profileService::setActiveProfile);
            } catch (Exception e) {
                // DB unavailable or profile deleted — start with no active profile
                // The UI already handles the "no active profile" state gracefully
                System.err.println("Could not restore last profile: " + e.getMessage());
            }
        }
    }

    public void shutdown() {
        saveLastProfile();
        calculatorClient.shutdown();
    }
}
