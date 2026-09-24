package com.example.saldo.service;

import com.example.saldo.dto.workspace.*;
import com.example.saldo.entity.*;
import com.example.saldo.exception.ConflictException;
import com.example.saldo.exception.NotFoundException;
import com.example.saldo.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;

    private final WalletRepository walletRepository;

    private final UserRepository userRepository;

    private final WorkspaceWalletRepository workspaceWalletRepository;

    private final WorkspaceMemberRepository workspaceMemberRepository;

    private final CategoryRepository categoryRepository;

    private final TransactionRepository transactionRepository;

    private final TransferRepository transferRepository;

    private final BudgetRepository budgetRepository;

    public List<WorkspaceResponse> getAllWorkspaces(UUID userId) {
        return workspaceRepository.findAllByUserId(userId).stream()
                .map(workspace -> new WorkspaceResponse(
                        workspace.getId(),
                        workspace.getName()
                ))
                .toList();
    }

    @Transactional
    public WorkspaceResponse createWorkspace(UUID userId, WorkspaceRequest workspaceRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException("User not found")
                );

        Workspace workspace = Workspace.builder()
                .name(workspaceRequest.name().trim())
                .build();
        Workspace saved = workspaceRepository.save(workspace);

        workspaceMemberRepository.save(WorkspaceMember.builder()
                .workspace(saved)
                .user(user)
                .role(Role.OWNER)
                .build());

        return new WorkspaceResponse(saved.getId(), saved.getName());
    }

    public List<MemberResponse> getMembers(UUID userId, UUID workspaceId) throws AccessDeniedException {
        requireMember(userId, workspaceId);

        return workspaceMemberRepository.findAllByWorkspaceId(workspaceId).stream()
                .map(member -> new MemberResponse(
                        member.getUser().getId(),
                        member.getUser().getLogin(),
                        member.getUser().getEmail(),
                        member.getRole()
                ))
                .toList();
    }

    @Transactional
    public MemberResponse inviteMember(
            UUID userId,
            UUID workspaceId,
            InviteRequest inviteRequest
    ) throws AccessDeniedException {
        if (!workspaceRepository.existsById(workspaceId)) {
            throw new NotFoundException("Workspace не найден");
        }

        requireMember(userId, workspaceId);

        String email = inviteRequest.email().trim().toLowerCase();
        User invited = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new NotFoundException("Пользователь с таким email не зарегистрирован")
                );

        if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, invited.getId())) {
            throw new ConflictException("Пользователь уже состоит в этом пространстве");
        }

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() ->
                        new NotFoundException("Workspace не найден")
                );

        WorkspaceMember saved = workspaceMemberRepository.save(WorkspaceMember.builder()
                .workspace(workspace)
                .user(invited)
                .role(Role.MEMBER)
                .build());

        return new MemberResponse(
                invited.getId(),
                invited.getLogin(),
                invited.getEmail(),
                saved.getRole()
        );
    }

    private void requireMember(UUID userId, UUID workspaceId) throws AccessDeniedException {
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new AccessDeniedException("Нет прав на данный workspace");
        }
    }

    public List<WalletResponse> getWorkspaceWallets(UUID workspaceId) {
        return walletRepository.findWalletsWithBalanceByWorkspaceId(workspaceId).stream()
                .map(wallet -> new WalletResponse(
                        wallet.getId(),
                        wallet.getName(),
                        wallet.getCurrency(),
                        wallet.getBalance()
                ))
                .toList();
    }

    @Transactional
    public WalletResponse createWallet(
            UUID userId,
            UUID workspaceId,
            WalletRequest walletRequest
    ) throws AccessDeniedException {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException("User not found")
                );

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Workspace not found")
                );

        boolean isMember =
                workspaceMemberRepository
                        .existsByWorkspaceIdAndUserId(
                                workspaceId,
                                userId
                        );

        if (!isMember) {
            throw new AccessDeniedException(
                    "User has no access to workspace"
            );
        }

        Wallet wallet = Wallet.builder()
                .name(walletRequest.name().trim())
                .owner(user)
                .initialBalance(walletRequest.initialBalance())
                .currency(walletRequest.currency().trim().toUpperCase())
                .build();

        Wallet createdWallet = walletRepository.save(wallet);

        WorkspaceWallet workspaceWallet =
                WorkspaceWallet.builder()
                        .workspace(workspace)
                        .wallet(createdWallet)
                        .build();

        workspaceWalletRepository.save(workspaceWallet);

        return new WalletResponse(
                createdWallet.getId(),
                createdWallet.getName(),
                createdWallet.getCurrency(),
                createdWallet.getInitialBalance()
        );
    }

    @Transactional
    public WalletResponse updateWallet(
            UUID userId,
            UUID workspaceId,
            UUID walletId,
            UpdateWalletRequest walletRequest
    ) throws AccessDeniedException {
        if (walletRequest.name() == null && walletRequest.initialBalance() == null) {
            throw new IllegalArgumentException("Нужно передать name или initialBalance");
        }

        if (!workspaceRepository.existsById(workspaceId)) {
            throw new NotFoundException("Workspace не найден");
        }

        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new AccessDeniedException("Нет прав на данный workspace");
        }

        if (!workspaceWalletRepository.existsByWorkspace_IdAndWallet_Id(workspaceId, walletId)) {
            throw new NotFoundException("Кошелёк не найден в этом workspace");
        }

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() ->
                        new NotFoundException("Кошелёк не найден в этом workspace")
                );

        if (walletRequest.name() != null) {
            String name = walletRequest.name().trim();
            if (name.isBlank()) {
                throw new IllegalArgumentException("Название кошелька не может быть пустым");
            }
            wallet.setName(name);
        }
        if (walletRequest.initialBalance() != null) {
            wallet.setInitialBalance(walletRequest.initialBalance());
        }

        Wallet saved = walletRepository.save(wallet);

        return walletWithBalance(workspaceId, saved);
    }

    private WalletResponse walletWithBalance(UUID workspaceId, Wallet wallet) {
        WalletBalanceProjection balance = walletRepository
                .findWalletsWithBalanceByWorkspaceId(workspaceId).stream()
                .filter(w -> w.getId().equals(wallet.getId()))
                .findFirst()
                .orElseThrow(() ->
                        new NotFoundException("Кошелёк не найден в этом workspace")
                );

        return new WalletResponse(
                wallet.getId(),
                wallet.getName(),
                wallet.getCurrency(),
                balance.getBalance()
        );
    }

    public List<OwnedWalletResponse> getMyWallets(UUID userId) {
        return walletRepository.findAllByOwnerId(userId).stream()
                .map(wallet -> new OwnedWalletResponse(
                        wallet.getId(),
                        wallet.getName(),
                        wallet.getCurrency()
                ))
                .toList();
    }

    @Transactional
    public WalletResponse attachWallet(
            UUID userId,
            UUID workspaceId,
            AttachWalletRequest attachRequest
    ) throws AccessDeniedException {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() ->
                        new NotFoundException("Workspace не найден")
                );

        requireMember(userId, workspaceId);

        Wallet wallet = walletRepository.findById(attachRequest.walletId())
                .filter(w -> w.getOwner().getId().equals(userId))
                .orElseThrow(() ->
                        new NotFoundException("Кошелёк не найден")
                );

        if (workspaceWalletRepository.existsByWorkspace_IdAndWallet_Id(workspaceId, wallet.getId())) {
            throw new ConflictException("Кошелёк уже подключён к этому пространству");
        }

        workspaceWalletRepository.save(WorkspaceWallet.builder()
                .workspace(workspace)
                .wallet(wallet)
                .build());

        return walletWithBalance(workspaceId, wallet);
    }

    public List<CategoryResponse> getAllCategories(UUID workspaceId) {
        return categoryRepository.findAllByWorkspaceId(workspaceId).stream()
                .map(category -> new CategoryResponse(
                        category.getId(),
                        category.getName(),
                        category.getType()
                ))
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(UUID userId, UUID workspaceId, CategoryRequest categoryRequest) throws AccessDeniedException {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() ->
                        new NotFoundException("Workspace не найден")
                );

        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new AccessDeniedException("Нет прав на данный workspace");
        }

        String name = categoryRequest.name().trim();

        if (categoryRepository.existsByWorkspaceIdAndNameIgnoreCaseAndType(
                workspaceId,
                name,
                categoryRequest.type()
        )) {
            throw new ConflictException("Данная категория уже существует");
        }

        Category category = Category.builder()
                .name(name)
                .type(categoryRequest.type())
                .workspace(workspace)
                .build();

        Category savedCategory = categoryRepository.save(category);

        return new CategoryResponse(
                savedCategory.getId(),
                savedCategory.getName(),
                savedCategory.getType()
        );
    }

    public PageResponse<TransactionResponse> getTransactions(
            UUID userId,
            UUID workspaceId,
            int page,
            int size
    ) throws AccessDeniedException {
        boolean hasAccess = workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId);

        if (!hasAccess) {
            throw new AccessDeniedException(
                    "User has no access to workspace"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "occurredAt"
                )
        );

        Page<Transaction> transactions = transactionRepository.findAllByWorkspace_Id(
                workspaceId,
                pageable
        );

        Page<TransactionResponse> response =
                transactions.map(transaction ->
                        new TransactionResponse(
                                transaction.getId(),
                                transaction.getWallet().getId(),
                                transaction.getWallet().getName(),
                                transaction.getCategory().getId(),
                                transaction.getCategory().getName(),
                                transaction.getCategory().getType(),
                                transaction.getAmount(),
                                transaction.getOccurredAt(),
                                transaction.getComment()
                        )
                );

        return new PageResponse<>(
                response.getContent(),
                response.getTotalElements(),
                response.getTotalPages(),
                response.getNumber()
        );
    }

    @Transactional
    public CategoryResponse updateCategory(
            UUID userId,
            UUID workspaceId,
            UUID categoryId,
            UpdateCategoryRequest categoryRequest
    ) throws AccessDeniedException {
        if (categoryRequest.name() == null && categoryRequest.type() == null) {
            throw new IllegalArgumentException("Нужно передать name или type");
        }

        if (!workspaceRepository.existsById(workspaceId)) {
            throw new NotFoundException("Workspace не найден");
        }

        if (!workspaceMemberRepository
                .existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new AccessDeniedException(
                    "Нет прав на данный workspace"
            );
        }

        Category category = categoryRepository
                .findByIdAndWorkspaceId(categoryId, workspaceId)
                .orElseThrow(() ->
                        new NotFoundException("Нет такой категории")
                );

        String newName = categoryRequest.name() != null
                ? categoryRequest.name().trim()
                : category.getName();
        CategoryType newType = categoryRequest.type() != null
                ? categoryRequest.type()
                : category.getType();

        if (newName.isBlank()) {
            throw new IllegalArgumentException("Название категории не может быть пустым");
        }

        boolean nameChanged = !newName.equalsIgnoreCase(category.getName())
                || !newType.equals(category.getType());
        if (nameChanged && categoryRepository.existsByWorkspaceIdAndNameIgnoreCaseAndType(
                workspaceId, newName, newType)) {
            throw new ConflictException("Данная категория уже существует");
        }

        category.setName(newName);
        category.setType(newType);

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType()
        );
    }

    @Transactional
    public void deleteCategory(
            UUID userId,
            UUID workspaceId,
            UUID categoryId
    ) throws AccessDeniedException {
        if (!workspaceRepository.existsById(workspaceId)) {
            throw new NotFoundException("Workspace не найден");
        }

        if (!workspaceMemberRepository
                .existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new AccessDeniedException(
                    "Нет прав на данный workspace"
            );
        }

        Category category = categoryRepository
                .findByIdAndWorkspaceId(categoryId, workspaceId)
                .orElseThrow(() ->
                        new NotFoundException("Нет такой категории")
                );

        if (transactionRepository.existsByCategoryId(categoryId)) {
            throw new ConflictException(
                    "Категория используется в транзакциях и не может быть удалена"
            );
        }

        categoryRepository.delete(category);
    }

    public TransactionResponse createTransaction(
            UUID userId,
            UUID workspaceId,
            TransactionRequest transactionRequest) throws AccessDeniedException {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() ->
                        new NotFoundException("Workspace не найден")
                );

        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new AccessDeniedException("Нет прав на данный workspace");
        }

        if (!workspaceWalletRepository.existsByWorkspace_IdAndWallet_Id(
                workspaceId, transactionRequest.walletId())) {
            throw new NotFoundException("Кошелёк не найден в этом workspace");
        }
        Wallet wallet = walletRepository.findById(transactionRequest.walletId())
                .orElseThrow(() ->
                        new NotFoundException("Кошелёк не найден в этом workspace")
                );

        Category category = categoryRepository
                .findByIdAndWorkspaceId(transactionRequest.categoryId(), workspaceId)
                .orElseThrow(() ->
                        new NotFoundException("Категория не найдена в этом workspace")
                );

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException("User not found")
                );

        Transaction transaction = Transaction.builder()
                .workspace(workspace)
                .wallet(wallet)
                .category(category)
                .createdBy(user)
                .amount(transactionRequest.amount())
                .occurredAt(transactionRequest.occurredAt())
                .comment(transactionRequest.comment())
                .build();

        Transaction saved = transactionRepository.save(transaction);

        return new TransactionResponse(
                saved.getId(),
                wallet.getId(),
                wallet.getName(),
                category.getId(),
                category.getName(),
                category.getType(),
                saved.getAmount(),
                saved.getOccurredAt(),
                saved.getComment()
        );
    }

    @Transactional
    public TransactionResponse updateTransaction(
            UUID userId,
            UUID workspaceId,
            UUID transactionId,
            UpdateTransactionRequest transactionRequest
    ) throws AccessDeniedException {
        if (transactionRequest.walletId() == null
                && transactionRequest.categoryId() == null
                && transactionRequest.amount() == null
                && transactionRequest.occurredAt() == null
                && transactionRequest.comment() == null) {
            throw new IllegalArgumentException("Нужно передать хотя бы одно поле");
        }

        if (!workspaceRepository.existsById(workspaceId)) {
            throw new NotFoundException("Workspace не найден");
        }

        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new AccessDeniedException("Нет прав на данный workspace");
        }

        Transaction transaction = transactionRepository
                .findByIdAndWorkspace_Id(transactionId, workspaceId)
                .orElseThrow(() ->
                        new NotFoundException("Транзакция не найдена")
                );

        if (transactionRequest.walletId() != null) {
            if (!workspaceWalletRepository.existsByWorkspace_IdAndWallet_Id(
                    workspaceId, transactionRequest.walletId())) {
                throw new NotFoundException("Кошелёк не найден в этом workspace");
            }
            Wallet wallet = walletRepository.findById(transactionRequest.walletId())
                    .orElseThrow(() ->
                            new NotFoundException("Кошелёк не найден в этом workspace")
                    );
            transaction.setWallet(wallet);
        }

        if (transactionRequest.categoryId() != null) {
            Category category = categoryRepository
                    .findByIdAndWorkspaceId(transactionRequest.categoryId(), workspaceId)
                    .orElseThrow(() ->
                            new NotFoundException("Категория не найдена в этом workspace")
                    );
            transaction.setCategory(category);
        }

        if (transactionRequest.amount() != null) {
            transaction.setAmount(transactionRequest.amount());
        }
        if (transactionRequest.occurredAt() != null) {
            transaction.setOccurredAt(transactionRequest.occurredAt());
        }
        if (transactionRequest.comment() != null) {
            transaction.setComment(transactionRequest.comment());
        }

        return new TransactionResponse(
                transaction.getId(),
                transaction.getWallet().getId(),
                transaction.getWallet().getName(),
                transaction.getCategory().getId(),
                transaction.getCategory().getName(),
                transaction.getCategory().getType(),
                transaction.getAmount(),
                transaction.getOccurredAt(),
                transaction.getComment()
        );
    }

    @Transactional
    public void deleteTransaction(
            UUID userId,
            UUID workspaceId,
            UUID transactionId
    ) throws AccessDeniedException {
        if (!workspaceRepository.existsById(workspaceId)) {
            throw new NotFoundException("Workspace не найден");
        }

        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new AccessDeniedException("Нет прав на данный workspace");
        }

        Transaction transaction = transactionRepository
                .findByIdAndWorkspace_Id(transactionId, workspaceId)
                .orElseThrow(() ->
                        new NotFoundException("Транзакция не найдена")
                );

        transactionRepository.delete(transaction);
    }

    @Transactional
    public TransferResponse createTransfer(
            UUID userId,
            UUID workspaceId,
            TransferRequest transferRequest
    ) throws AccessDeniedException {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() ->
                        new NotFoundException("Workspace не найден")
                );

        requireMember(userId, workspaceId);

        if (transferRequest.fromWalletId().equals(transferRequest.toWalletId())) {
            throw new IllegalArgumentException("Кошельки источника и назначения должны различаться");
        }

        Wallet fromWallet = linkedWallet(workspaceId, transferRequest.fromWalletId());
        Wallet toWallet = linkedWallet(workspaceId, transferRequest.toWalletId());

        if (!fromWallet.getCurrency().equals(toWallet.getCurrency())) {
            throw new IllegalArgumentException("Переводы между разными валютами пока не поддерживаются");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException("User not found")
                );

        Transfer transfer = Transfer.builder()
                .workspace(workspace)
                .fromWallet(fromWallet)
                .toWallet(toWallet)
                .createdBy(user)
                .amount(transferRequest.amount())
                .occurredAt(transferRequest.occurredAt())
                .comment(transferRequest.comment())
                .build();

        Transfer saved = transferRepository.save(transfer);

        return new TransferResponse(
                saved.getId(),
                fromWallet.getId(),
                fromWallet.getName(),
                toWallet.getId(),
                toWallet.getName(),
                saved.getAmount(),
                saved.getOccurredAt(),
                saved.getComment()
        );
    }

    public PageResponse<TransferResponse> getTransfers(
            UUID userId,
            UUID workspaceId,
            int page,
            int size
    ) throws AccessDeniedException {
        requireMember(userId, workspaceId);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "occurredAt")
        );

        Page<Transfer> transfers = transferRepository.findAllByWorkspace_Id(workspaceId, pageable);

        Page<TransferResponse> response = transfers.map(transfer ->
                new TransferResponse(
                        transfer.getId(),
                        transfer.getFromWallet().getId(),
                        transfer.getFromWallet().getName(),
                        transfer.getToWallet().getId(),
                        transfer.getToWallet().getName(),
                        transfer.getAmount(),
                        transfer.getOccurredAt(),
                        transfer.getComment()
                )
        );

        return new PageResponse<>(
                response.getContent(),
                response.getTotalElements(),
                response.getTotalPages(),
                response.getNumber()
        );
    }

    private Wallet linkedWallet(UUID workspaceId, UUID walletId) {
        if (!workspaceWalletRepository.existsByWorkspace_IdAndWallet_Id(workspaceId, walletId)) {
            throw new NotFoundException("Кошелёк не найден в этом workspace");
        }
        return walletRepository.findById(walletId)
                .orElseThrow(() ->
                        new NotFoundException("Кошелёк не найден в этом workspace")
                );
    }

    @Transactional
    public BudgetItemResponse createBudget(
            UUID userId,
            UUID workspaceId,
            BudgetRequest budgetRequest
    ) throws AccessDeniedException {
        if (!workspaceRepository.existsById(workspaceId)) {
            throw new NotFoundException("Workspace не найден");
        }

        requireMember(userId, workspaceId);

        Category category = categoryRepository
                .findByIdAndWorkspaceId(budgetRequest.categoryId(), workspaceId)
                .orElseThrow(() ->
                        new NotFoundException("Категория не найдена в этом workspace")
                );

        YearMonth period = parsePeriod(budgetRequest.period());

        if (budgetRepository.existsByWorkspaceIdAndCategoryIdAndPeriod(
                workspaceId, category.getId(), period)) {
            throw new ConflictException("Бюджет на эту категорию и месяц уже существует");
        }

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() ->
                        new NotFoundException("Workspace не найден")
                );

        Budget saved = budgetRepository.save(Budget.builder()
                .workspace(workspace)
                .category(category)
                .period(period)
                .amount(budgetRequest.amount())
                .build());

        return new BudgetItemResponse(
                saved.getId(),
                category.getId(),
                category.getName(),
                saved.getAmount(),
                factForCategory(workspaceId, period, category.getId())
        );
    }

    @Transactional
    public BudgetItemResponse updateBudget(
            UUID userId,
            UUID workspaceId,
            UUID budgetId,
            UpdateBudgetRequest budgetRequest
    ) throws AccessDeniedException {
        if (!workspaceRepository.existsById(workspaceId)) {
            throw new NotFoundException("Workspace не найден");
        }

        requireMember(userId, workspaceId);

        Budget budget = budgetRepository
                .findByIdAndWorkspaceId(budgetId, workspaceId)
                .orElseThrow(() ->
                        new NotFoundException("Бюджет не найден")
                );

        budget.setAmount(budgetRequest.amount());

        return new BudgetItemResponse(
                budget.getId(),
                budget.getCategory().getId(),
                budget.getCategory().getName(),
                budget.getAmount(),
                factForCategory(workspaceId, budget.getPeriod(), budget.getCategory().getId())
        );
    }

    @Transactional
    public void deleteBudget(
            UUID userId,
            UUID workspaceId,
            UUID budgetId
    ) throws AccessDeniedException {
        if (!workspaceRepository.existsById(workspaceId)) {
            throw new NotFoundException("Workspace не найден");
        }

        requireMember(userId, workspaceId);

        Budget budget = budgetRepository
                .findByIdAndWorkspaceId(budgetId, workspaceId)
                .orElseThrow(() ->
                        new NotFoundException("Бюджет не найден")
                );

        budgetRepository.delete(budget);
    }

    public BudgetOverviewResponse getBudgetOverview(
            UUID userId,
            UUID workspaceId,
            String periodParam
    ) throws AccessDeniedException {
        if (!workspaceRepository.existsById(workspaceId)) {
            throw new NotFoundException("Workspace не найден");
        }

        requireMember(userId, workspaceId);

        YearMonth period = periodParam != null ? parsePeriod(periodParam) : YearMonth.now();

        Map<UUID, BigDecimal> facts = new HashMap<>();
        for (CategorySpending spending : transactionRepository.sumExpensesByCategory(
                workspaceId,
                period.atDay(1).atStartOfDay(),
                period.plusMonths(1).atDay(1).atStartOfDay())) {
            facts.put(spending.getCategoryId(), spending.getTotal());
        }

        List<BudgetItemResponse> items = budgetRepository
                .findAllByWorkspaceIdAndPeriod(workspaceId, period).stream()
                .map(budget -> new BudgetItemResponse(
                        budget.getId(),
                        budget.getCategory().getId(),
                        budget.getCategory().getName(),
                        budget.getAmount(),
                        facts.getOrDefault(budget.getCategory().getId(), BigDecimal.ZERO)
                ))
                .toList();

        BigDecimal totalPlanned = items.stream()
                .map(BudgetItemResponse::planned)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalFact = items.stream()
                .map(BudgetItemResponse::fact)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new BudgetOverviewResponse(period.toString(), items, totalPlanned, totalFact);
    }

    private BigDecimal factForCategory(UUID workspaceId, YearMonth period, UUID categoryId) {
        return transactionRepository.sumExpensesByCategory(
                        workspaceId,
                        period.atDay(1).atStartOfDay(),
                        period.plusMonths(1).atDay(1).atStartOfDay()).stream()
                .filter(s -> s.getCategoryId().equals(categoryId))
                .map(CategorySpending::getTotal)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    private YearMonth parsePeriod(String period) {
        try {
            return YearMonth.parse(period);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Период — в формате yyyy-MM");
        }
    }
}
